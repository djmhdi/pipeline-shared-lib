#!/usr/bin/groovy

import static fr.pe.jenkins.plugins.bundle.BundleFactory.*
import fr.pe.jenkins.plugins.util.*
import fr.pe.jenkins.plugins.notification.*

def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    check(config)

    def delivery
    try {
        String[] bundlesGAV = createListGAVFromBundle(config.bundles)
        delivery = deliver(bundlesGAV, config.matrice)
        env['APPROD_ID'] = delivery.getApproId()
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
            config.notifications[i].appendData(NotificationDataKeys.DELIVERY.key, delivery)
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

def check(def config) {
    if (!config.matrice) {
        throw new Exception("le parametre 'matrice' est obligatoire !")
    }
    if (!config.bundles) {
        throw new Exception("le parametre 'bundles' est obligatoire !")
    }

    if (!config.notifications) {
        config.notifications = []
    }

    if (!config.retryTimeout) {
        config.retryTimeout = 3
    }
}

def deliver(String[] bundlesGAV, String matriceGav) {
    def delivery
    echo "\n============== Livraison  ========================="
    echo "-- matrix : $matriceGav"
    echo "-- bundles : ${'[' + bundlesGAV.join('; ') + ']'}"

    delivery = proven.deltaDeliverMatrix {
        gav = "${matriceGav}"
        deltas = bundlesGAV
    }

    echo "-- Appro Id : ${delivery.approId}"
    echo "-- Appro Url : ${delivery.approUrl}"
    echo "-- Appro log : ${delivery.logUrl}"
    echo "-- Appro status : ${delivery.status}"

    if (delivery.hasFailed()) {
        error "ERROR : ${delivery.matrix.gav} [${delivery.approId}] - La livraison a échoué - ${delivery.logUrl}"
    }

    echo "==================================================="
    return delivery
}
