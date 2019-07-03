#!/usr/bin/groovy
import fr.pe.jenkins.plugins.bundle.BundleFactory
import fr.pe.jenkins.plugins.bundle.Bundle
import fr.pe.jenkins.plugins.util.*
import fr.pe.jenkins.plugins.notification.*
import fr.pe.jenkins.plugins.notification.library.*
import fr.pe.jenkins.plugins.notification.simple.*

/**
 *
 * @param body
 *        - CONFIGURATION_JSON ou CONFIGURATION_FILE (resultat d'un readFile configuration.json ou d'un objet groovy construire sur le même modèle)
 *        - OPTIONS_CADRE (ex : "TIC\nTIS\nVA-1\nVA-2\nPROD")
 *        - PROD_ENVNAMES (ex: ['PROD', 'PMS', 'BEC'])
 *        - ASK_FOR_DELIVERY (envoyer la demande de notification mattermost et mail)
 *        - NEED_APPROVAL (nécessite la validation par un livreur)
 *        - RESTART_BUNDLES_PREFIX (groupId:Artifactid des bundles dont les serveurs doivent être redémarrés)
 * @return
 */
def call(body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def configuration
    String bundleMatrix
    Set bundlesGAV = []
    def bundleList = []
    String allBundlesLabel = "Tous"
    def cadre
    int defaultInputTimeout = 15
    boolean manualInstallation

    def userProven
    def mdpProven

    pipeline {
        agent none

        environment {
            provenUser = "${env['PROVEN_user']}"
            provenPassword = "${env['PROVEN_password']}"

        tools {
            jdk "JAVA_1_8_0_66_HOME"
        }

        options {
            buildDiscarder(logRotator(numToKeepStr: '20'))
        }

        parameters {
            choice(name: 'CADRE', choices: config.OPTIONS_CADRE, description: 'Choix du cadre')
            booleanParam(name: 'DEPLOYER', defaultValue: false, description: 'Déploiement manuel du bundle')
            booleanParam(name: 'ASK_FOR_DELIVERY', defaultValue: true, description: 'Doit passer par une demande de livraison')
            booleanParam(name: 'NEED_APPROVAL', defaultValue: true, description: 'La livraison doit faire)
        }

        stages {
            stage('prepare') {
                agent {
                    label 'maven'
                }
                steps {
                    script {
                        cadre = "$params.CADRE"
                        manualInstallation = params.DEPLOYER
                        bundlesGAV.add(allBundlesLabel)
                        userProven = env.provenUser
                        mdpProven = env.provenPassword

                        echo "Demande de livraison sur l'environnement $cadre"

                        // Initialisation des variables propre au stage
                        def jsonConfig
                        if (config.CONFIGURATION_JSON) {
                            jsonConfig = config.CONFIGURATION_JSON
                        }
                        if (config.CONFIGURATION_FILE) {
                            try {
                                jsonConfig = readFile(config.CONFIGURATION_FILE)
                            } catch (exception) {
                                echo "Configuration de la livraison en erreur : " + exception.getMessage()
                                throw exception
                            }
                        }

                        configuration = json.parseFromText(jsonConfig)
                        if (configuration.environnements[cadre] == null) {
                            throw new RuntimeException("Aucun cadre '${cadre}' déclaré dans la configuration des environnements")
                        }

                        bundleMatrix = configuration.environnements[cadre].matrix

                        // Affichage du titre du build
                        currentBuild.displayName = "Livraison $params.CADRE #${env['BUILD_NUMBER']}"
                    }
                }
            }

            stage("Demande de livraison") {
                agent none
                when {
                    expression { return config.ASK_FOR_DELIVERY }
                }
                steps {
                    script {
                        def pvnMatrix = proven.getMatrix { gav = bundleMatrix }
                        for (def elt in pvnMatrix.matrixElements) {
                            bundlesGAV.add(elt.bundleGAV)
                        }

                        env.SELECTED_BUNDLE = input message: 'Bundle à livrer', ok: 'Go !',
                                parameters: [choice(name: 'SELECTED_BUNDLE', choices: bundlesGAV.join('\n'), description: 'Sélectionner le bundle à livrer ?')]

                        /* provenMatrix.matrixElements.findResult{ it.bundleGAV.startsWith("${bundleGAV}")?it.getDictionaryGAV():null }*/

                        if (env.SELECTED_BUNDLE == allBundlesLabel) {
                            for (def bundleGAV in bundlesGAV) {
                                if (bundleGAV != allBundlesLabel) {
                                    bundleList.add(BundleFactory.createBundleFromGAV(bundleGAV))
                                }
                            }
                        } else {
                            bundleList.add(BundleFactory.createBundleFromGAV(bundleGAV))
                        }

                        currentBuild.description = bundleList.join('\n')

                        Notification demandeLivraisonNotif = DemandeLivraison.to(configuration.contacts.LIV, "${configuration.contacts.ST}, ${configuration.contacts.EQUIPE}")
                                .by([NotificationProtocol.MAIL, NotificationProtocol.SLACK])
                                .withSlackOpts(configuration.team.endpoint, configuration.team.channel)
                        demandeLivraisonNotif.appendData(NotificationDataKeys.PROJECT_NAME.key, configuration.projet)
                        demandeLivraisonNotif.appendData(NotificationDataKeys.DESCRIPTION.key, "[$configuration.projet] Demande de livraison ${cadre}")
                        demandeLivraisonNotif.appendData(NotificationDataKeys.BUNDLES.key, bundleList)
                        demandeLivraisonNotif.appendData(NotificationDataKeys.CADRE.key, "${cadre}")
                        demandeLivraisonNotif.appendData(NotificationDataKeys.MATRICE.key, bundleMatrix)

                        demanderLivraison {
                            submitters = configuration.submitters.DEMANDE_LIVRAISON
                            notifications = [demandeLivraisonNotif]
                            inputTimeout = defaultInputTimeout
                        }
                    }
                }
            }

            stage("Confirmer livraison") {
                agent none
                when {
                    expression { return config.NEED_APPROVAL }
                }
                steps {
                    try {
                        timeout(time: defaultInputTimeout, unit: 'DAYS') {
                            input message: "Valider la livraison ?", ok: 'Livrer !', submitter: configuration.submitters.LIVRAISON
                        }
                    } catch (exception) {
                        currentBuild.result = 'ABORTED'
                        throw exception
                    }
                }
            }

            stage("Livraison") {
                agent {
                    label 'maven'
                }
                steps {
                    script {
                        // Si le cadre sélectionné est le cadre de production, on demande au livreur de saisir ces identifiants Proven
                        // Sinon, on récupère les identifiants génériques configurés dans l'environnement d'exécution
                        if (config.PROD_ENVNAMES.contains(cadre)) {
                            def result = input message: "Production - Renseigner votre login Neptune", ok: 'Ok !',
                                    parameters: [[$class: 'TextParameterDefinition', defaultValue: "", description: '', name: 'Proven_User'],
                                                 [$class: 'PasswordParameterDefinition', description: 'Mot de passe Neptune', name: 'Proven_Password']]
                            userProven = result['Proven_User']
                            mdpProven = result['Proven_Password']
                        }

                        Notification livraisonNotif = Livraison.to(configuration.contacts.EQUIPE, configuration.contacts.ST).on([NotificationLevel.FAILURE.level])
                                .by([NotificationProtocol.MAIL, NotificationProtocol.SLACK])
                                .withSlackOpts(configuration.team.endpoint, configuration.team.channel)
                        livraisonNotif.appendData(NotificationDataKeys.PROJECT_NAME.key, configuration.projet)
                        livraisonNotif.appendData(NotificationDataKeys.CADRE.key, cadre)
                        livraisonNotif.appendData(NotificationDataKeys.DESCRIPTION.key, "[$configuration.projet] Livraison ${cadre}")

                        withEnv(["provenUser="+userProven,"provenPassword=" +mdpProven]) {
                            echo "Livraison avec le user : ${env.provenUser}"
                            livrer {
                                bundles = bundleList
                                matrice = bundleMatrix
                                notifications = [livraisonNotif]
                            }
                        }

                        if (manualInstallation) {
                            Notification deploiementNotif = Deploiement.to(configuration.contacts.EQUIPE, configuration.contacts.ST)
                                    .by([NotificationProtocol.MAIL, NotificationProtocol.SLACK])
                                    .withSlackOpts(configuration.team.endpoint, configuration.team.channel)
                            deploiementNotif.appendData(NotificationDataKeys.PROJECT_NAME.key, configuration.projet)
                            deploiementNotif.appendData(NotificationDataKeys.CADRE.key, cadre)
                            deploiementNotif.appendData(NotificationDataKeys.MATRICE.key, bundleMatrix)

                            deployer {
                                bundles = bundleList
                                matrice = bundleMatrix
                                notifications = [deploiementNotif]
                            }

                            if (config.RESTART_BUNDLES_PREFIX) {
                                def pvnMatrix = proven.getMatrix {
                                    gav = bundleMatrix
                                }
                                def bundlesToRestart = ProvenResource.bundlesGAVFromMatrix(bundleMatrix, config.RESTART_BUNDLE_PREFIX)

                                redemarrer {
                                    matrice = configuration.environnements[cadre].matrix
                                    bundles = bundlesToRestart
                                }
                            }
                        }
                    }

                }
            }
        }
    }
}
