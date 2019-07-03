#!/usr/bin/groovy
package fr.pe.jenkins.plugins.notification.library

import com.cloudbees.groovy.cps.NonCPS
import fr.pe.jenkins.plugins.notification.*
import fr.pe.jenkins.plugins.util.*
import groovy.text.SimpleTemplateEngine

import java.util.logging.Logger

class Livraison extends AbstractNotification {

    static Logger logger = Logger.getLogger('fr.pe.jenkins.plugins.notification.library.Livraison')

    private Livraison() {
        defaultContentModelName = DefaultNotificationTemplate.LIVRAISON.name
        defaultTitleModel = ''' [\$projectName][\$cadre][\$env.customBuildResult] Livraison \$component '''
    } 
        
    private Livraison(String recipientsList, String recipientsCcList) {
        this()
        this.recipients = recipientsList
        this.recipientsCc = recipientsCcList 
    }

    @NonCPS
    static to(String recipients, String recipientsCc) {
        return new Livraison(recipients, recipientsCc)
    }

    /**
     * A utiliser pour des notifications mattermost-only
     * @return
     */
    @NonCPS
    static slack() {
        return new Livraison().by([NotificationProtocol.SLACK])
    }
}
