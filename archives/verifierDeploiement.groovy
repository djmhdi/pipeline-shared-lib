#!/usr/bin/groovy

import com.capgemini.fs.jenkins.plugins.bundle.Bundle
import com.capgemini.fs.jenkins.plugins.notification.NotificationDataKeys
import com.capgemini.fs.jenkins.plugins.util.JenkinsNodes
import static com.capgemini.fs.jenkins.plugins.bundle.BundleFactory.*
import static com.capgemini.fs.jenkins.plugins.deploy.CheckDeployment.*
import static com.capgemini.fs.jenkins.plugins.proven.ProvenResource.dictionaryGavFor

def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    check(config)

    if (!config.inputTimeout) {
        // 7 jours, car après les fichiers fadia disparaissent
        config.inputTimeout = 7
    }

    if (!config.verificationAuto) {
        try {
            timeout(time: config.inputTimeout, unit: 'DAYS') {
                input message: "Lancer la vérification du déploiement ?", ok: 'Vérifier', submitter: config.submitters
            }
        } catch (exception) {
            currentBuild.result = 'ABORTED'
            throw exception
        }
    }

    def retryCheck = true
    while (retryCheck) {
        retryCheck = false // relivraison que s'il y a une exception
        echo "Verifie le déploiement."

        try {
            def pvnMatrix = proven.getMatrix {
                gav = config.matrice
            }
            for (Bundle bundle : config.bundles) {
                checkFadiaDeployment(this, pvnMatrix, bundle, env['APPROD_ID'])
                checkDeployment(this, pvnMatrix, bundle)
            }
            env['customBuildResult'] = 'SUCCESS'
        }
        catch (err) {
            env['customBuildResult'] = 'FAILURE'
            def w = new StringWriter()
            err.printStackTrace(new PrintWriter(w))
            echo "[MESSAGE]:${err.message}${System.getProperty("line.separator")}${w.toString()}}"
            w = null
            retryCheck = true
        }
        finally {
            // On boucle sur les notifications et on y ajoute les infos
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
    if (retryCheck) {
        //retry ?
        try {
            timeout(time: config.retryTimeout, unit: 'HOURS') {
                input message: "Vérification en Echec !", ok: "Relancer la vérification", submitter: config.submitters
            }
        } catch (exception) {
            currentBuild.result = 'ABORTED'
            throw exception
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