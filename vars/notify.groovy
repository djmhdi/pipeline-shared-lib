#!/usr/bin/groovy

import com.capgemini.fs.jenkins.plugins.notification.*
import com.capgemini.fs.jenkins.plugins.util.JenkinsHelper

/**
 * Fonction de notification par différents protocols
 * @param ( m a n d a t o r y ) config.notifications liste de notifications contenues dans le package com.capgemini.fs.jenkins.plugins.notification
 * @param ( optional ) config.status statut du build
 */
def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    if (!config.status) {
        config.status = currentBuild.currentResult
    }

    if (!config.notifications) {
        config.notifications = []
    }

    def signature = JenkinsHelper.latestActorMessage(currentBuild)

    for (int i = 0; i < config.notifications.size(); i++) {
        def notification = config.notifications[i]

        notification.appendData(NotificationDataKeys.CURRENT_BUILD.key, currentBuild)
        notification.appendData(NotificationDataKeys.ENV.key, env)
        notification.appendData(NotificationDataKeys.SIGNATURE.key, signature)

        String component = ""
        if (notification.exists(NotificationDataKeys.MATRICE)) {
            component = "matrice ${notification.getData(NotificationDataKeys.MATRICE)}"
        }
        if (notification.exists(NotificationDataKeys.BUNDLES) && notification.getData(NotificationDataKeys.BUNDLES).size() == 1) {
            component = "bundle ${notification.getData(NotificationDataKeys.BUNDLES)[0].artifactId}:${notification.getData(NotificationDataKeys.BUNDLES)[0].releaseVersion}"
        }
        notification.appendData(NotificationDataKeys.COMPONENT.key, component)

        /** Dans le cas de ressources propres à la library - libraryResource **/
        notification.setupTemplates(this)

        NotificationLevel configStatus = NotificationLevel.getFromLevel(config.status)
        echo "notifier: configStatus = ${configStatus.toString()} vs notifLevel = ${notification.level.toString()}"

        try {
            if (configStatus == null || configStatus >= notification.level) {
                if (notification.protocols.contains(NotificationProtocol.MAIL)) {
                    sendHtmlEmail(notification)
                }

                if (notification.protocols.contains(NotificationProtocol.SLACK)) {
                    sendMattermost(notification)
                }

                if (notification.protocols.contains(NotificationProtocol.LYNC)) {
                    echo "La fonction 'notifier' ne supporte pas encore le protocol ${NotificationProtocol.LYNC}"
                }
            }

        } catch (Exception exception) {
            def findProperty = (exception.getMessage() =~ /No such property: ([a-zA-Z])* /)
            def propertyName = "Unknown"

            if (findProperty) {
                def result = findProperty[0][0].tokenize(":")
                propertyName = result[1]
            }

            echo "!! Il manque la propriété ''${propertyName}'' pour enrichir le modèle de notification ${notification.defaultContentModelName} !!"

            throw exception
        }
    }
}

def sendHtmlEmail(AbstractNotification notification) {
    notification.buildFor(this, NotificationProtocol.MAIL)

    if ((notification.recipients != null && !notification.recipients.isEmpty()) || (notification.recipientsCc != null && !notification.recipientsCc.isEmpty())) {
        echo "Mail sent to:$notification.recipients and cc:$notification.recipientsCc"
        mail to: notification.recipients, cc: notification.recipientsCc, mimeType: 'text/html', subject: "$notification.subject", body: "$notification.content"
    } else {
        echo "WARN : emailDestinataires is Empty ! No email sent !"
    }
}

def sendMattermost(AbstractNotification notification) {
    notification.buildFor(this, NotificationProtocol.SLACK)
    def colors = [:]
    colors['SUCCESS'] = "good"
    colors['FAILURE'] = "danger"
    colors['UNSTABLE'] = "warning"
    colors['ABORTED'] = "#E14706"
    colors['NOT_BUILT'] = "#A3A3A3"


    if (!notification.slackColor) {
        notification.withColor(colors[currentBuild.currentResult])
    }

    if (notification.isSlackOptsOverrided()) {
        mattermostSend endpoint: notification.slackEndpoint, channel: notification.slackChannel, color: notification.slackColor, message: "$notification.content", text: "$notification.subject", icon: notification.slackIcon
    } else {
        mattermostSend channel: '#town-square', color: notification.slackColor, message: "$notification.content", text: "$notification.subject", icon: notification.slackIcon
    }

}
