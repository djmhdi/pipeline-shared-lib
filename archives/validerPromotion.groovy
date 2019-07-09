#!/usr/bin/groovy
import com.capgemini.fs.jenkins.plugins.notification.NotificationDataKeys
import com.capgemini.fs.jenkins.plugins.bundle.BundleFactory
import com.capgemini.fs.jenkins.plugins.util.*

def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    check(config)

    try {
        timeout(time: config.inputTimeout, unit: 'DAYS') {
            input message:"Promotion en ${config.promotion.name()} ?", ok: 'Go !', submitter:config.submitters
        }
    } catch (exception) {
        currentBuild.result = 'ABORTED'
        throw exception
    }
}

def check(def config) {
    if (!config.submitters) {
        throw new Exception("le parametre 'submitters' est obligatoire !")
    }
    if (!config.promotion) {
        throw new Exception("le parametre 'promotion' est obligatoire !")
    }

    if (!config.inputTimeout) {
        config.inputTimeout = 15
    }
}
