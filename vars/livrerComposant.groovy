#!/usr/bin/groovy
import fr.pe.jenkins.plugins.bundle.BundleFactory
import fr.pe.jenkins.plugins.bundle.Bundle
import fr.pe.jenkins.plugins.util.*
import fr.pe.jenkins.plugins.proven.ProvenResource
import fr.pe.jenkins.plugins.notification.*
import fr.pe.jenkins.plugins.notification.library.*
import fr.pe.jenkins.plugins.notification.simple.*

/**
 *
 * @param body
 *        - CONFIGURATION_JSON ou CONFIGURATION_FILE (resultat d'un readFile configuration.json ou d'un objet groovy construire sur le même modèle)
 *        - OPTIONS_CADRE (ex : "TIC\nTIS\nVA-1\nVA-2\nPROD")
 *        - PROD_ENVNAMES (ex: ['PROD', 'PMS', 'BEC'])
 * @return
 */
def call(body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    String cadre
    String bundleMatrix
    int defaultInputTimeout = 15
    boolean manualInstallation
    def bundleToDeliver
    def configuration = [:]

    def userProven
    def mdpProven

    pipeline {
        agent none

        environment {
            provenUser = "${env['PROVEN_user']}"
            provenPassword = "${env['PROVEN_password']}"
        }

        options {
            buildDiscarder(logRotator(numToKeepStr: '20'))
        }

        tools {
            jdk "JAVA_1_8_0_66_HOME"
        }

        parameters {
            choice(name: 'CADRE', choices: config.OPTIONS_CADRE, description: 'Choix du cadre')
            string(name: 'CIBLE_LIVRAISON', defaultValue: '', description: 'Numéro de VSI - Numéro de changement')
            string(name: 'BUNDLE_GAV', defaultValue: '', description: 'groupId:ArtifactId:version')
            string(name: 'RESTART_BUNDLES_PREFIX', defaultValue: '', description: 'groupId|:artifactId*')
        }

        stages {
            stage('Preparation') {
                agent {
                    label 'maven'
                }
                steps {
                    script {
                        cadre = "$params.CADRE"
                        userProven = env.provenUser
                        mdpProven = env.provenPassword

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

                        manualInstallation = configuration.environnements[cadre].deploy
                        bundleMatrix = configuration.environnements[cadre].matrix
                        bundleToDeliver = BundleFactory.createBundleFromGAV(params.BUNDLE_GAV)

                        // Affichage du titre du build
                        currentBuild.displayName = "#${env['BUILD_NUMBER']} Livraison $params.CADRE"
                        currentBuild.description = params.BUNDLE_GAV

                        echo "====================== Livraison du composant ${params.BUNDLE_GAV}"
                        echo "====================== Livraison avec la matrice ${bundleMatrix}"
                    }
                }
            }

            stage("Notification livraison") {
                agent none
                when {
                    not {
                        expression { return manualInstallation }
                    }
                }
                steps {
                    script {
                        Notification demanderLivraisonNotif = DemandeLivraison.to(configuration.contacts.LIV, "${configuration.contacts.ST}, ${configuration.contacts.EQUIPE}")
                                .by([NotificationProtocol.MAIL, NotificationProtocol.SLACK])
                                .withSlackOpts(configuration.team.endpoint, configuration.team.channel)
                        demanderLivraisonNotif.appendData(NotificationDataKeys.PROJECT_NAME.key, configuration.projet)
                        demanderLivraisonNotif.appendData(NotificationDataKeys.DESCRIPTION.key, "[${params.CIBLE_LIVRAISON}] Demande de livraison en ${cadre}")
                        demanderLivraisonNotif.appendData(NotificationDataKeys.BUNDLES.key, [bundleToDeliver])
                        demanderLivraisonNotif.appendData(NotificationDataKeys.CADRE.key, cadre)
                        demanderLivraisonNotif.appendData(NotificationDataKeys.MATRICE.key, configuration.environnements[cadre].matrix)

                        notifier {
                            notifications = [demanderLivraisonNotif]
                        }
                    }
                }
            }

            stage("Autorisation livraison") {
                agent none
                when {
                    not {
                        expression { return manualInstallation }
                    }
                }
                steps {
                    timeout(time: defaultInputTimeout, unit: 'DAYS') {
                        input message: "Valider la livraison ?", ok: 'Livrer !', submitter: configuration.submitters.LIVRAISON
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
                        boolean isProductionEnv  = configuration.environnements[cadre].production || ((config.PROD_ENVNAMES != null)&& config.PROD_ENVNAMES.contains(cadre))
                        if (isProductionEnv) {
                            def result = input message: "Production - Renseigner votre login Neptune", ok: 'Ok !',
                                    parameters: [[$class: 'TextParameterDefinition', defaultValue: "", description: 'Login Neptune', name: 'Proven_User'],
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

                        withEnv(["provenUser=" + userProven, "provenPassword=" + mdpProven]) {
                            echo "Livraison avec le user : ${env.provenUser}"
                            livrer {
                                bundles = [bundleToDeliver]
                                matrice = bundleMatrix
                                notifications = [livraisonNotif]
                            }

                            if (isProductionEnv) {
                                sauvegarderMatrice {
                                    matrice = bundleMatrix
                                    bundles = [bundleToDeliver]
                                }
                            }
                        }

                        if (manualInstallation) {
                            Notification deploiementNotif = Deploiement.to(configuration.contacts.EQUIPE, configuration.contacts.ST)
                                    .by([NotificationProtocol.MAIL, NotificationProtocol.SLACK])
                                    .withSlackOpts(configuration.team.endpoint, configuration.team.channel)
                            deploiementNotif.appendData(NotificationDataKeys.PROJECT_NAME.key, configuration.projet)
                            deploiementNotif.appendData(NotificationDataKeys.CADRE.key, cadre)
                            deploiementNotif.appendData(NotificationDataKeys.MATRICE.key, configuration.environnements[cadre].matrix)

                            deployer {
                                bundles = [bundleToDeliver]
                                matrice = bundleMatrix
                                notifications = [deploiementNotif]
                            }

                            if (params.RESTART_BUNDLES_PREFIX) {
                                def pvnMatrix = proven.getMatrix {
                                    gav = bundleMatrix
                                }

                                def bundlesToRestart = ProvenResource.bundlesGAVFromMatrix(pvnMatrix, params.RESTART_BUNDLES_PREFIX)

                                redemarrer {
                                    matrice = bundleMatrix
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
