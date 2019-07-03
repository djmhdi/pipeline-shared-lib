#!/usr/bin/groovy
package fr.pe.jenkins.plugins.notification.simple

import com.cloudbees.groovy.cps.NonCPS
import fr.pe.jenkins.plugins.notification.*
import fr.pe.jenkins.plugins.util.*
import groovy.text.SimpleTemplateEngine

import java.util.logging.Logger

class CustomNotification extends AbstractNotification {

	static Logger logger = Logger.getLogger('fr.pe.jenkins.plugins.notification.simple.CustomNotification')

    	private CustomNotification() {
    	}

	private CustomNotification(String recipientsList, String recipientsCcList) {
		this.recipients = recipientsList
		this.recipientsCc = recipientsCcList
	}

    Notification with(NotificationProtocol notificationProtocol, String subjectModel, String contentModel) {
        setTemplate (notificationProtocol, NotificationTemplate.with(subjectModel, contentModel))

        return this
    }

    /**
     *  this.datas.each { key, value ->
     *   binding[key] = value
     * };
     */
    @NonCPS
	static to(String recipients, String recipientsCc) {
		return new CustomNotification(recipients, recipientsCc)
	}

    /**
     * A utiliser pour des notifications mattermost-only
     * @return
     */
    @NonCPS
    static slack() {
        return new CustomNotification().by([NotificationProtocol.SLACK])
    }

    public void setupTemplates(script) {
        /**  do nothing for custom notification **/
    }
}
