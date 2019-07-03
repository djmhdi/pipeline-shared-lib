#!/usr/bin/groovy
package fr.pe.jenkins.plugins.notification.library

import com.cloudbees.groovy.cps.NonCPS
import fr.pe.jenkins.plugins.notification.*
import fr.pe.jenkins.plugins.util.*
import groovy.text.SimpleTemplateEngine

import java.util.logging.Logger

class ConstructionSnapshot extends AbstractNotification {

	static Logger logger = Logger.getLogger('fr.pe.jenkins.plugins.notification.library.ConstructionSnapshot')

	private ConstructionSnapshot() {
		defaultContentModelName = DefaultNotificationTemplate.BUILD_SNAPSHOT.name
		defaultTitleModel = ''' [\$projectName][\$currentBuild.currentResult] Build snapshot \$bundle.artifactId:\$bundle.releaseVersion '''
	}

	private ConstructionSnapshot(String recipientsList, String recipientsCcList) {
		this()
		this.recipients = recipientsList
		this.recipientsCc = recipientsCcList
	}
				
    @NonCPS
	static to(String recipients, String recipientsCc) {
		return new ConstructionSnapshot(recipients, recipientsCc)
	}

	@NonCPS
	static slack() {
		return new ConstructionSnapshot().by([NotificationProtocol.SLACK])
	}
}
