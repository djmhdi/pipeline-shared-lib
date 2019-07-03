#!/usr/bin/groovy
import fr.pe.jenkins.plugins.notification.NotificationDataKeys
import fr.pe.jenkins.plugins.quality.*

def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    if (env.NODE_NAME) throw new Exception("Should not be in a node !!!")
    check(config)

    try {
        timeout(time: config.sonarTimeout, unit: 'MINUTES') {
            def qg = waitForQualityGate()
            println "INFO: Sonar Quality Gate Status: ${qg.status} (${config.strategie})"

            if (qg.status != 'OK') {
                if (config.strategie >= QualityStrategy.ABORT_ANYWAY) {
                    println "Pipeline aborted due to quality gate issues: ${qg.status}"
                    currentBuild.result = "FAILURE"
                } else if (config.strategie >= QualityStrategy.UNSTABLE_ANYWAY) {
                    println "Pipeline unstable due to quality gate : ${qg.status}"
                    currentBuild.result = "UNSTABLE"
                } else {
                    if (qg.status == 'FAILED') {
                        println "Pipeline failed due to quality gate failures: ${qg.status}"
                        currentBuild.result = "FAILURE"

                    } else {
                        println "Pipeline unstable due to quality gate issues: ${qg.status}"
                        currentBuild.result = "UNSTABLE"
                    }
                }
            }
        }
    }
    catch (exception) {
        currentBuild.result = "ABORTED"
        throw exception
    }
    finally {
        // On boucle sur les notifications et on y ajoute les infos
        for (int i = 0; i < config.notifications.size() ; i++) {
            config.notifications[i].appendData(NotificationDataKeys.BUNDLE.key, config.bundle)
            config.notifications[i].appendData(NotificationDataKeys.SONAR.key, json.parseFromText(env['MYSONAR_PROJECT_PROPS']))
        }
        notifier {
            notifications = config.notifications
        }
    }
}

def check(def config) {
    if (config.notifications == null) {
        throw new Exception("le parametre 'notifications' est obligatoire ! Mais il peut etre vide.")
    }

    if (!config.bundle && config.notifications.size() > 0) {
        throw new Exception("le parametre 'bundle' est obligatoire avec les notifications !")
    }

    if (!config.sonarTimeout) {
        config.sonarTimeout = 10
    }

    if (!config.strategie) {
        config.strategie = QualityStrategy.STANDARD
    }

    if (!config.notifications) {
        config.notifications = []
    }
}
