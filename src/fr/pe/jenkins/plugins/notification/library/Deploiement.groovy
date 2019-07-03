#!/usr/bin/groovy
package fr.pe.jenkins.plugins.notification.library

import com.cloudbees.groovy.cps.NonCPS
import groovy.text.SimpleTemplateEngine
import java.util.logging.Logger

import fr.pe.jenkins.plugins.notification.*
import fr.pe.jenkins.plugins.util.*

class Deploiement extends AbstractNotification {

	static Logger logger = Logger.getLogger('fr.pe.jenkins.plugins.notification.library.Deploiement')

	private Deploiement() {
		defaultContentModelName = DefaultNotificationTemplate.DEPLOIEMENT.name
		defaultTitleModel = ''' [\$projectName][\$cadre][\$env.customBuildResult] Déploiement \$component '''
	}

	private Deploiement(String recipientsList, String recipientsCcList) {
		this()
		this.recipients = recipientsList
		this.recipientsCc = recipientsCcList
	}
				
    @NonCPS
	static to(String recipients, String recipientsCc) {
		return new Deploiement(recipients, recipientsCc)
	}

	/**
	 * A utiliser pour des notifications mattermost-only
	 * @return
	 */
	@NonCPS
	static slack() {
		return new Deploiement().by([NotificationProtocol.SLACK])
	}
}
