#!/usr/bin/groovy
package fr.pe.jenkins.plugins.notification

import com.cloudbees.groovy.cps.NonCPS
import java.util.logging.Logger
import groovy.text.SimpleTemplateEngine
import groovy.lang.MissingPropertyException
import fr.pe.jenkins.plugins.util.JenkinsHelper

abstract class AbstractNotification implements Notification {

    static Logger logger = Logger.getLogger('fr.pe.jenkins.plugins.notification.AbstractNotification')

    public String recipients, recipientsCc
    protected List<NotificationProtocol> protocols = [NotificationProtocol.MAIL]
    protected NotificationLevel level = NotificationLevel.SUCCESS
    protected String slackEndpoint, slackChannel
    protected String slackColor
    protected String slackIcon = "https://www.cloudbees.com/sites/default/files/blog/butler.png"
    protected boolean slackOptionsOverrided = false
    protected def datas = [:]
    protected def templates = [:]
    protected String subject, content

    protected String defaultTitleModel, defaultContentModelName

    @Override
    public def getData(NotificationDataKeys data) {
        return this.datas[data.key]
    }

    @Override
    public boolean exists(NotificationDataKeys data) {
        return this.datas.containsKey(data.key)
    }

    @Override
    Notification withColor(String thisColor) {
        this.slackColor = thisColor
        return this
    }

    @Override
    Notification withIcon(String thisIcon) {
        this.slackIcon = thisIcon
        return this
    }

    @Override
    Notification withSlackOpts(String endpoint, String channel) {
        this.slackEndpoint = endpoint
        this.slackChannel = channel
        this.slackOptionsOverrided = true

        return this
    }

    @Override
    boolean isSlackOptsOverrided() {
        return this.slackOptionsOverrided
    }

    @Override
    Notification by(List protocols) {
        if (protocols == null) {
            throw new IllegalArgumentException("Le paramètre ne doit pas être null !")
        }
        this.protocols.clear()
        for (int i = 0; i < protocols.size(); i++) {
            this.protocols.add(protocols[i])
        }
        return this
    }

    // On prend le plus petit level dans la liste
    @Deprecated
    @Override
    Notification on(List levels) {
        if (levels == null || levels.isEmpty()) {
            throw new IllegalArgumentException("Le paramètre ne doit être ni null, ni vide !")
        } else {
            this.level = NotificationLevel.getFromLevel(levels[0])
            for (int i = 1; i < levels.size(); i++) {
                if (this.level > NotificationLevel.getFromLevel(levels[i])) {
                    this.level = NotificationLevel.getFromLevel(levels[i])
                }
            }
        }
        return this
    }

    @Override
    Notification on(NotificationLevel level) {
        this.level = level
        return this
    }

    protected void setTemplate(NotificationProtocol notificationProtocol, NotificationTemplate template) {
        this.templates[notificationProtocol.name] = template
    }

    @Override
    void appendData(String key, def value) {
        this.datas[key] = value
    }

    @Override
    void appendDatas(Map thisDatas) {
        thisDatas.each { key, value ->
            this.appendData(key, value)
        }
    }

    /**
     * Compilation du template avec les données fournies en entrée
     * @param script le script appelant
     * @param dataMap les données requises sous la forme d'une map
     * @return Notification
     */
    public void setupTemplates(script) {
        NotificationProtocol notificationProtocol = null
        def contentModel = null

        for (int i = 0; i < protocols.size(); i++) {
            notificationProtocol = protocols[i]
            contentModel = script.libraryResource "fr/pe/notification/${notificationProtocol.name}/${defaultContentModelName}.tmpl"

            setTemplate(notificationProtocol, NotificationTemplate.with(this.defaultTitleModel, contentModel))
        }
    }

    /**
     * Compilation du template avec les données fournies en entrée
     * @param script le script appelant
     * dataMap = ['projectName', 'bundle', 'currentBuild', 'env']
     * @return Notification
     */
    @NonCPS
    public void buildFor(script, NotificationProtocol protocol) {
        NotificationTemplate template = templates[protocol.name]

        if (!template) {
            script.echo "Template inexistant pour le protocol $protocol.name !"
        }

        def binding = [:]

        this.datas.each { key, value ->
            binding[key] = value
        }

        def templateEngine = new SimpleTemplateEngine()

        this.subject = templateEngine.createTemplate(template.subjectModel).make(binding).toString()
        binding[NotificationDataKeys.TITLE.key] = this.subject

        this.content = templateEngine.createTemplate(template.contentModel).make(binding).toString()
    }

    @Override
    @NonCPS
    public String toString() {
        return "AbstractNotification{" +
                "recipients='" + recipients + '\'' +
                ", recipientsCc='" + recipientsCc + '\'' +
                ", subject='" + subject + '\'' +
                ", content='" + content + '\'' +
                ", protocols=" + Arrays.toString(protocols) +
                ", level=" + level.toString() +
                ", datas=" + datas +
                '}';
    }
}
