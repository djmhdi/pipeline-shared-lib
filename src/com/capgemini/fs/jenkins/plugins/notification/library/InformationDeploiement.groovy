#!/usr/bin/groovy
package com.capgemini.fs.jenkins.plugins.notification.library

import com.cloudbees.groovy.cps.NonCPS
import com.capgemini.fs.jenkins.plugins.notification.*
import com.capgemini.fs.jenkins.plugins.util.*
import groovy.text.SimpleTemplateEngine

import java.util.logging.Logger

class InformationDeploiement extends AbstractNotification {

	static Logger logger = Logger.getLogger('com.capgemini.fs.jenkins.plugins.notification.html.InformationDeploiement')

	private InformationDeploiement() {
		defaultContentModelName = DefaultNotificationTemplate.INFO_DEPLOIEMENT.name
		defaultTitleModel = ''' Information livraison [\$projectName]}][\$bundle.releaseVersion}][\$cadre] '''
	}

	private InformationDeploiement(String recipientsList, String recipientsCcList) {
        this()
		this.recipients = recipientsList
		this.recipientsCc = recipientsCcList
	}
				
    @NonCPS
    static to(String recipients, String recipientsCc) {
        return new InformationDeploiement(recipients, recipientsCc)
    }

    @NonCPS
    static to(String recipients) {
        return new InformationDeploiement(recipients, null)
    }

	/**
	 * A utiliser pour des notifications mattermost-only
	 * @return
	 */
	@NonCPS
	static slack() {
		return new InformationDeploiement().by([NotificationProtocol.SLACK])
	}
}
