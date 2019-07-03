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
class NotificationLivraisonSpec extends PipelineSpockTestBase {
    String titreLivraison
    def datas= [:]

    def setup() {
        titreLivraison = "[${projectName}][${CADRE_TIC}][${RESULT_SUCCESS}] Livraison bundle ${bundle.artifactId}:${bundle.releaseVersion}"
        datas[NotificationDataKeys.BUNDLE.key] = bundle
        datas[NotificationDataKeys.PROJECT_NAME.key] = projectName
        datas[NotificationDataKeys.BUNDLES.key] = [bundle]
        datas[NotificationDataKeys.CADRE.key] = 'TIC'
        datas[NotificationDataKeys.DESCRIPTION.key] = 'Livraison TIC'
        datas[NotificationDataKeys.PROMOTION_LEVEL.key] = 'RC'
        datas[NotificationDataKeys.MATRICE.key] = matrice
        datas[NotificationDataKeys.DELIVERY.key] = delivery
    }

    def cleanup() { target.clear() }

    def "L'implémentation Legacy LivraisonHtml est toujours opérationnelle"() {
        given:
            binding.getVariable('env').customBuildResult = RESULT_SUCCESS
            binding.getVariable('env').JOB_URL = DEFAULT_JOB_URL
            binding.getVariable('env').JOB_NAME = 'TestNotificationLivraison'
        when:
            Notification livraisonLegacy = LivraisonHtml.to(mailto, mailto)

            livraisonLegacy.appendDatas(datas)

            def closureNotifier = {
                notifications = [livraisonLegacy]
            }
            notifier.call(closureNotifier)
        then:
            livraisonLegacy.subject.contains(titreLivraison)
        then:
            printCallStack()
            assertJobStatusSuccess()
    }

    def "La notification Livraison gère deux protocoles et le titre est correct"() {
        given:
            binding.getVariable('env').customBuildResult = RESULT_SUCCESS
            binding.getVariable('env').JOB_URL = DEFAULT_JOB_URL
            binding.getVariable('env').JOB_NAME = 'TestNotificationLivraison'
        when:
            Notification livraison = Livraison.to(mailto, mailto).by([NotificationProtocol.MAIL, NotificationProtocol.SLACK])

            livraison.appendDatas(datas)

            def closureNotifier = {
                notifications = [livraison]
            }
            notifier.call(closureNotifier)
        then:
            livraison.subject.contains(titreLivraison)
        then:
            printCallStack()
            assertJobStatusSuccess()
    }

    def "La fabrique slack de Livraison gère uniquement le protocole slack"() {
        given:
            binding.getVariable('env').customBuildResult = RESULT_SUCCESS
            binding.getVariable('env').JOB_URL = DEFAULT_JOB_URL
            binding.getVariable('env').JOB_NAME = 'TestNotificationLivraison'
        when:
            Notification livraisonSlack = Livraison.slack()

            livraisonSlack.appendDatas(datas)

            def closureNotifier = {
                notifications = [livraisonSlack]
            }
            notifier.call(closureNotifier)
        then:
         livraisonSlack.subject.contains(titreLivraison)
        then:
            printCallStack()
            assertJobStatusSuccess()
    }
}
