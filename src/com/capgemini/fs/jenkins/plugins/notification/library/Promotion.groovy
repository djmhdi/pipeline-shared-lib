#!/usr/bin/groovy
package com.capgemini.fs.jenkins.plugins.notification.library

import com.cloudbees.groovy.cps.NonCPS
import com.capgemini.fs.jenkins.plugins.notification.*
import com.capgemini.fs.jenkins.plugins.util.*
import groovy.text.SimpleTemplateEngine

import java.util.logging.Logger

class Promotion extends AbstractNotification {

	static Logger logger = Logger.getLogger('com.capgemini.fs.jenkins.plugins.notification.library.Promotion')

	private Promotion() {
		defaultContentModelName = DefaultNotificationTemplate.PROMOTION.name
		defaultTitleModel = ''' [\$projectName] Promotion en \$promotionLevel '''
	}

	private Promotion(String recipientsList, String recipientsCcList) {
        this()
		this.recipients = recipientsList
		this.recipientsCc = recipientsCcList
	}
				
    @NonCPS
	static to(String recipients, String recipientsCc) {
		return new Promotion(recipients, recipientsCc)
	}

    /**
     * A utiliser pour des notifications mattermost-only
     * @return
     */
    @NonCPS
    static slack() {
        return new Promotion().by([NotificationProtocol.SLACK])
    }
}
