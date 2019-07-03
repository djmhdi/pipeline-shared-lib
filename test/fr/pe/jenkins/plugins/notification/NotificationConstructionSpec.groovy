package fr.pe.jenkins.plugins.notification

import spock.lang.*
import fr.pe.jenkins.plugins.notification.*
import fr.pe.jenkins.plugins.notification.html.*
import fr.pe.jenkins.plugins.notification.library.*
import fr.pe.jenkins.plugins.notification.simple.*
import fr.pe.jenkins.plugins.bundle.*
import fr.pe.jenkins.plugins.spock.PipelineSpockTestBase
import java.util.logging.Logger
import org.slf4j.*
import groovy.util.logging.Slf4j

/**
 * Demonstrates how &#64;Stepwise causes a spec to be run in incremental steps.
 * Change a step's condition from <tt>true</tt> to <tt>false</tt>, and observe
 * how the remaining steps will be skipped automatically on the next run.
 * Also notice that if you run a single step (e.g. from the IDE's context menu),
 * all prior steps will also be run.
 *
 * <p><tt>&#64;Stepwise</tt> is particularly useful for higher-level specs whose
 * methods have logical dependencies.
 *
 * @since 0.4
 */
@Stepwise
@Slf4j
class NotificationConstructionSpec extends PipelineSpockTestBase {
    String titreConstruction
    def datas= [:]

    def setup() {
        titreConstruction = "[${projectName}][${RESULT_SUCCESS}] Build release ${bundle.artifactId}:${bundle.releaseVersion}"

        datas[NotificationDataKeys.BUNDLE.key] = bundle
        datas[NotificationDataKeys.PROJECT_NAME.key] = projectName
    }

    def cleanup() { target.clear() }

    def "L'implémentation Legacy ConstructionVersionHtml est toujours opérationnelle"() {
        given: "L'environnement contient la propriété customBuildResultat avec la valeur SUCCESS"
            binding.getVariable('env').customBuildResult = RESULT_SUCCESS
            binding.getVariable('env').JOB_URL = DEFAULT_JOB_URL
            binding.getVariable('env').JOB_NAME = 'TestNotificationRelease'
        when: "On utilise la fabrique 'to' simple"

            Notification constructionLegacy = ConstructionVersionHtml.to(mailto, mailto)

        and : "On ajoute les données requises - BUNDLE et PROJECT_NAME"
            constructionLegacy.appendDatas(datas)

            def closureNotifier = {
                notifications = [constructionLegacy]
            }

        and : "On appelle la fonction globale 'notifier'"
            notifier.call(closureNotifier)
        then: "Le sujet de la notification est formaté convenablement"
            constructionLegacy.subject.contains(titreConstruction)
        then: "Le job s'exécute correctement"
            printCallStack()
            assertJobStatusSuccess()
    }

    def "La notification ConstructionVersion gère deux protocoles et le titre est correct"() {
        given: "L'environnement contient la propriété customBuildResultat avec la valeur SUCCESS"
            binding.getVariable('env').customBuildResult = RESULT_SUCCESS
            binding.getVariable('env').JOB_URL = DEFAULT_JOB_URL
            binding.getVariable('env').JOB_NAME = 'TestNotificationRelease'
        when: "On utilise la fabrique 'to' avec les protocoles SLACK et MAIL"
            Notification construction = ConstructionVersion.to(mailto, mailto).by([NotificationProtocol.MAIL, NotificationProtocol.SLACK])

            construction.appendDatas(datas)

            def closureNotifier = {
                notifications = [construction]
            }
            notifier.call(closureNotifier)
        then: "Le sujet de la notification est formaté convenablement"
            construction.subject.contains(titreConstruction)
        then: "Le job s'exécute correctement"
            printCallStack()
            assertJobStatusSuccess()
    }

    def "La fabrique slack de ConstructionVersion gère uniquement le protocole slack"() {
        given: "L'environnement contient la propriété customBuildResultat avec la valeur SUCCESS"
            binding.getVariable('env').customBuildResult = RESULT_SUCCESS
            binding.getVariable('env').JOB_URL = DEFAULT_JOB_URL
            binding.getVariable('env').JOB_NAME = 'TestNotificationRelease'
        when: "On utilise la fabrique slack seule"
            Notification constructionSlack = ConstructionVersion.slack()

            constructionSlack.appendDatas(datas)

            def closureNotifier = {
                notifications = [constructionSlack]
            }
            notifier.call(closureNotifier)
        then: "Le sujet de la notification est formaté convenablement"
            constructionSlack.subject.contains(titreConstruction)
        then: "Le job s'exécute correctement"
            printCallStack()
            assertJobStatusSuccess()
    }
}
