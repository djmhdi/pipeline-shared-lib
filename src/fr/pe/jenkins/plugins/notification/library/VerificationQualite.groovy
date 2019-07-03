#!/usr/bin/groovy
package fr.pe.jenkins.plugins.notification.library

import com.cloudbees.groovy.cps.NonCPS
import fr.pe.jenkins.plugins.notification.*
import fr.pe.jenkins.plugins.util.*
import groovy.text.SimpleTemplateEngine

import java.util.logging.Logger

class VerificationQualite extends AbstractNotification {

	static Logger logger = Logger.getLogger('fr.pe.jenkins.plugins.notification.library.VerificationQualite')

	private VerificationQualite() {
		defaultContentModelName = DefaultNotificationTemplate.VERIFICATION_QUALITE.name
		defaultTitleModel = ''' [\$projectName][\$currentBuild.currentResult] Vérification Qualite bundle \$bundle.artifactId:\$bundle.releaseVersion '''
	}

	private VerificationQualite(String recipientsList, String recipientsCcList) {
		this()
		this.recipients = recipientsList
		this.recipientsCc = recipientsCcList
	}
				
    @NonCPS
	static to(String recipients, String recipientsCc) {
		return new VerificationQualite(recipients, recipientsCc)
	}

    /**
     * A utiliser pour des notifications mattermost-only
     * @return
     */
    @NonCPS
    static slack() {
        return new VerificationQualite().by([NotificationProtocol.SLACK])
    }
}
