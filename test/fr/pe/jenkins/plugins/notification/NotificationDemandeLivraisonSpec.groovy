package com.capgemini.fs.jenkins.plugins.notification

import spock.lang.*
import com.capgemini.fs.jenkins.plugins.notification.*
import com.capgemini.fs.jenkins.plugins.notification.html.*
import com.capgemini.fs.jenkins.plugins.notification.library.*
import com.capgemini.fs.jenkins.plugins.notification.simple.*
import com.capgemini.fs.jenkins.plugins.bundle.*
import com.capgemini.fs.jenkins.plugins.spock.PipelineSpockTestBase
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
class NotificationDemandeLivraisonSpec extends PipelineSpockTestBase {
    String titreDemandeLivraison
    def datas = [:]

    def setup() {
        titreDemandeLivraison = "[${projectName}][${CADRE_TIC}] Demande livraison bundle ${bundle.artifactId}:${bundle.releaseVersion}"

        datas[NotificationDataKeys.PROJECT_NAME.key] = projectName
        datas[NotificationDataKeys.BUNDLES.key] = [bundle]
        datas[NotificationDataKeys.CADRE.key] = CADRE_TIC
        datas[NotificationDataKeys.DESCRIPTION.key] = 'Description demande livraison TIC'
    }

    def cleanup() { target.clear() }

    def "L'implémentation Legacy DemandeLivraisonHtml est toujours opérationnelle"() {
        given:
            binding.getVariable('env').customBuildResult = RESULT_SUCCESS
            binding.getVariable('env').JOB_URL = DEFAULT_JOB_URL
            binding.getVariable('env').JOB_NAME = 'TestNotificationDemandeLivraison'
        when:
            Notification dlivLegacy = DemandeLivraisonHtml.to(mailto, mailto)

            dlivLegacy.appendDatas(datas)

            def closureNotifier = {
                notifications = [dlivLegacy]
            }
            notifier.call(closureNotifier)
        then:
            dlivLegacy.subject.contains(titreDemandeLivraison)
        then:
            printCallStack()
            assertJobStatusSuccess()
    }

    def "La notification DemandeLivraison gère deux protocoles et le titre est correct"() {
        given:
            binding.getVariable('env').customBuildResult = RESULT_SUCCESS
            binding.getVariable('env').JOB_URL = DEFAULT_JOB_URL
            binding.getVariable('env').JOB_NAME = 'TestNotificationDemandeLivraison'
        when:
            Notification dliv = DemandeLivraison.to(mailto, mailto).by([NotificationProtocol.MAIL, NotificationProtocol.SLACK])

            dliv.appendDatas(datas)

            def closureNotifier = {
                notifications = [dliv]
            }
            notifier.call(closureNotifier)
        then:
         dliv.subject.contains(titreDemandeLivraison)
        then:
            printCallStack()
            assertJobStatusSuccess()
    }

    def "La fabrique slack de Livraison gère uniquement le protocole slack"() {
        given:
            binding.getVariable('env').customBuildResult = RESULT_SUCCESS
            binding.getVariable('env').JOB_URL = DEFAULT_JOB_URL
            binding.getVariable('env').JOB_NAME = 'TestNotificationDemandeLivraison'
        when:
            Notification dlivSlack = DemandeLivraison.slack()

            dlivSlack.appendDatas(datas)

            def closureNotifier = {
                notifications = [dlivSlack]
            }
            notifier.call(closureNotifier)
        then:
            dlivSlack.subject.contains(titreDemandeLivraison)
        then:
            printCallStack()
            assertJobStatusSuccess()
    }
}
