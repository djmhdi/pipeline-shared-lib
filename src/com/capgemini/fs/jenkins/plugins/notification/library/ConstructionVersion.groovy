#!/usr/bin/groovy
package com.capgemini.fs.jenkins.plugins.notification.library

import com.cloudbees.groovy.cps.NonCPS
import com.capgemini.fs.jenkins.plugins.notification.*
import com.capgemini.fs.jenkins.plugins.util.*
import groovy.text.SimpleTemplateEngine

import java.util.logging.Logger

class ConstructionVersion extends AbstractNotification {

	static Logger logger = Logger.getLogger('com.capgemini.fs.jenkins.plugins.notification.library.ConstructionVersion')

    private ConstructionVersion() {
		defaultContentModelName = DefaultNotificationTemplate.CONSTRUCTION.name
		defaultTitleModel = ''' [\$projectName][\$currentBuild.currentResult] Build release \$bundle.artifactId:\$bundle.releaseVersion '''
    }

	private ConstructionVersion(String recipientsList, String recipientsCcList) {
		this()
		this.recipients = recipientsList
		this.recipientsCc = recipientsCcList
	}

    /**
     * A utiliser en cas de notification mail-and-more ou mail-almost
     * @param recipientsList
     * @param recipientsCcList
     * @return
     */
    @NonCPS
	static to(String recipientsList, String recipientsCcList) {
		return new ConstructionVersion(recipientsList, recipientsCcList)
	}

    /**
     * A utiliser pour des notifications mattermost-only
     * @return
     */
    @NonCPS
    static slack() {
        return new ConstructionVersion().by([NotificationProtocol.SLACK])
    }
}
