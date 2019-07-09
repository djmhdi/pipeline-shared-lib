#!/usr/bin/groovy
package com.capgemini.fs.jenkins.plugins.notification.library

import com.cloudbees.groovy.cps.NonCPS
import com.capgemini.fs.jenkins.plugins.notification.*
import com.capgemini.fs.jenkins.plugins.util.*
import groovy.text.SimpleTemplateEngine

import java.util.logging.Logger

class DemandeLivraison extends AbstractNotification {

	static Logger logger = Logger.getLogger('com.capgemini.fs.jenkins.plugins.notification.html.DemandeLivraison')

	private DemandeLivraison() {
		defaultContentModelName = DefaultNotificationTemplate.DEMANDE_LIVRAISON.name
		defaultTitleModel = ''' [\$projectName][\$cadre] Demande livraison \$component '''
	}

	private DemandeLivraison(String recipientsList, String recipientsCcList) {
        this()
		this.recipients = recipientsList
		this.recipientsCc = recipientsCcList
	}
				
    @NonCPS
	static to(String recipients, String recipientsCc) {
		return new DemandeLivraison(recipients, recipientsCc)
	}

	/**
	 * A utiliser pour des notifications mattermost-only
	 * @return
	 */
	@NonCPS
	static slack() {
		return new DemandeLivraison().by([NotificationProtocol.SLACK])
	}
}
