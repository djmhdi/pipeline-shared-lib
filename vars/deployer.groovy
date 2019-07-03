#!/usr/bin/groovy
import fr.pe.jenkins.plugins.notification.NotificationDataKeys

import static fr.pe.jenkins.plugins.deploy.CheckDeployment.*
import static fr.pe.jenkins.plugins.deploy.PrepaDeployConso.*
import static fr.pe.jenkins.plugins.bundle.BundleFactory.*
import fr.pe.jenkins.plugins.util.*
import fr.pe.jenkins.plugins.bundle.Bundle
import fr.pe.jenkins.plugins.proven.ProvenResource

import static fr.pe.jenkins.plugins.restart.Restart.restart

/**
 * Permet de déployer un ou plusieurs bundles sur un environnement
 * config.bundles : un tableau de bundles. ex: bundles = [releaseBundle]
 * config.environnement : l'environnement sur lequel déployé. ex : environnement.TIC en considérant qu'environnement
 * est un objet JSON, issu d'un fichier ou non, respectant la structure
 *{*    "TIC": {*        "matrix": "fr.pe.tech.test:jenkins-pipeline-test-fab:tic",
 *}*}*/
def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    check(config)

    def pvnMatrix = null

    echo "Redémarrage lancé pour la matrice  :" + config.matrice
    try {
        pvnMatrix = proven.getMatrix {
            gav = config.matrice
        }
        echo "Matrix                : ${pvnMatrix.toString()} "
        for (Bundle bundleDeployed : config.bundles) {
            echo "Deploiement du Bundle             : ${bundleDeployed.releaseGAV}"
            deployerBundle(pvnMatrix, bundleDeployed)
        }
        env['customBuildResult'] = 'SUCCESS'
    } catch (err) {
        env['customBuildResult'] = 'FAILURE'
        def w = new StringWriter()
        err.printStackTrace(new PrintWriter(w))
        echo "[MESSAGE]:${err.message}${System.getProperty("line.separator")}${w.toString()}}"
        w = null
        throw err
    } finally {
        // On boucle sur les notifications et on y ajoute le resultat du deliver proven
        for (int i = 0; i < config.notifications.size(); i++) {
            config.notifications[i].appendData(NotificationDataKeys.BUNDLES.key, config.bundles)
            config.notifications[i].appendData(NotificationDataKeys.MATRICE.key, config.matrice)
        }
        def buildStatus = env['customBuildResult']
        notifier {
            notifications = config.notifications
            status = buildStatus
        }
    }
}

def renameFadiaFilesToOrd(def pvnMatrix, String approId, Bundle bundle) {
    if (!approId || !pvnMatrix || !bundle) {
        echo "WARNING! Pas d'approId ou de matrice ou de bundle, donc pas de renommage des fichiers d'install fadia !"
        return
    }

    // Récupération du code appli et de la lettre de cadre
    String dictionaryGAV = ProvenResource.dictionaryGavFor(bundle.getReleaseGAV(), pvnMatrix)
    echo "Recherche du dictionnaire pour le GAV : ${dictionaryGAV}"
    def dictionary = proven.getDictionary {
        gav = "${dictionaryGAV}"
    }
    String appli = dictionary["fadia.appli"]
    String cadre = dictionary["fadia.cadre"]

    // Définition du pattern pour récupérer le fichier fadia
    String currentExtension = '.ec'
    String pseudoBundleGAV = bundle.releaseGAV.replaceAll('\\.', '-').replaceAll(':', '-').replaceAll('_', '-')
    String installFilePattern = "^install.*${pseudoBundleGAV}-${approId}_${cadre}...\\${currentExtension}\$"
    echo "installFilePattern: $installFilePattern"

    // Renommage
    def pvnTargets = targetsDescriptorFor bundle.releaseGAV, pvnMatrix
    for (int i = 0; i < pvnTargets.size(); i++) {
        def target = pvnTargets[i]
        String fadiaFileName = sh(
                script: """ ssh -l xjenki ${target.host} "ls /sasech/x${appli}/pur/liv | { grep -E -i -w '${
                    installFilePattern
                }' || true; }" """,
                returnStdout: true
        )
        fadiaFileName = fadiaFileName.trim()
        if (!fadiaFileName) {
            echo "INFO! Sur la machine ${target.host} pour le bundle ${bundle.releaseGAV} et l'appro ${approId}, pas de fichier d'install fadia en .ec trouvé  !"
        } else {
            String fadiaFilePath = "/sasech/x${appli}/pur/liv/${fadiaFileName}"
            String nextFadiaFilePath = fadiaFilePath.substring(0, fadiaFilePath.size() - currentExtension.length()) + '.ord'
            sh(
                    script: """ ssh -l xjenki ${target.host} "mv ${fadiaFilePath} ${nextFadiaFilePath}" """,
                    returnStdout: false
            )
        }
    }
}

def check(def config) {
    if (config.bundles?.size() < 1) {
        throw new Exception("le parametre 'bundles' est obligatoire !")
    }

    if (!config.matrice) {
        throw new Exception("le parametre 'matrice' est obligatoire !")
    }

    if (!config.notifications) {
        config.notifications = []
    }

    if (!config.retryTimeout) {
        config.retryTimeout = 3
    }
}

def deployerBundle(def pvnMatrix, Bundle bundleDeployed) {
    echo "Recherche du gav du dictionnaire pour le bundle : ${bundleDeployed.releaseGAV}"
    String dictionaryGAV = ProvenResource.dictionaryGavFor(bundleDeployed.releaseGAV, pvnMatrix)

    echo "Recherche du dictionnaire pour le GAV : ${dictionaryGAV}"
    def dictionary = proven.getDictionary {
        gav = "${dictionaryGAV}"
    }

    def targets = ProvenResource.targetsDescriptorFor(bundleDeployed.releaseGAV, pvnMatrix)
    echo "Dictionary            : $dictionaryGAV"

    for (def target : targets) {
        deploy(this, bundleDeployed, target, dictionary, '')
        /*restart(this, target, dictionary)
        checkDeployment(this, pvnMatrix, bundleDeployed)*/
    }
}

