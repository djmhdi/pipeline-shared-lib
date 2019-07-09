#!/usr/bin/groovy
import com.capgemini.fs.jenkins.plugins.util.HtmlTemplates

import com.capgemini.fs.jenkins.plugins.util.*

def call(body) {
	// evaluate the body block, and collect configuration into the object
	def config = [:]
	body.resolveStrategy = Closure.DELEGATE_FIRST
	body.delegate = config
	body()

	if (!config.inputTimeout) {
		config.inputTimeout = 15
	}
    def cibleLivraison
	try {
		timeout (time:config.inputTimeout, unit: 'DAYS') {
            int trimestre = (Calendar.getInstance(Locale.FRANCE).get(Calendar.MONTH) / 3) + 1;
            String inputDefaultValue = new Date().format('yyyy') + "SI$trimestre"

            cibleLivraison = input(
                    id: 'cibleLivraison', submitter: config.submitters, message: "Envoyer la demande de livraison ?",
                    parameters: [[$class: 'TextParameterDefinition', defaultValue: inputDefaultValue, description: 'Au format <Version cible>-<changement>', name: 'Cible']])
		}
	} catch (exception) {
		currentBuild.result = 'ABORTED'
		throw exception
	}

    // facultatif
	notifier {
		notifications = config.notifications
	}

    return cibleLivraison
}