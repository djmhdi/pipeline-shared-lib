#!/usr/bin/groovy
package fr.pe.jenkins.plugins.notification.library

import com.cloudbees.groovy.cps.NonCPS
import fr.pe.jenkins.plugins.notification.*
import fr.pe.jenkins.plugins.util.*
import groovy.text.SimpleTemplateEngine

import java.util.logging.Logger

class VerificationDeploiement extends AbstractNotification {

	static Logger logger = Logger.getLogger('fr.pe.jenkins.plugins.notification.library.VerificationDeploiement')

	private VerificationDeploiement() {
		defaultContentModelName = DefaultNotificationTemplate.VERIFICATION_DEPLOIEMENT.name
		defaultTitleModel = ''' [\$projectName][\$cadre][\$env.customBuildResult] Vérification déploiement \$component '''
	}

	private VerificationDeploiement(String recipientsList, String recipientsCcList) {
        this()
		this.recipients = recipientsList
		this.recipientsCc = recipientsCcList
	}
				
    @NonCPS
	static to(String recipients, String recipientsCc) {
		return new VerificationDeploiement(recipients, recipientsCc)
	}

	@NonCPS
	static slack() {
		return new VerificationDeploiement().by([NotificationProtocol.SLACK])
	}
}
