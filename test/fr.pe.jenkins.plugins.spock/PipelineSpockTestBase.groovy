package com.capgemini.fs.jenkins.plugins.spock

/**
 * Created by eggo1060 on 04/10/2017.
 */
import com.lesfurets.jenkins.unit.RegressionTest
import spock.lang.Specification
import com.capgemini.fs.jenkins.plugins.bundle.*

/**
 * A base class for Spock testing using the pipeline helper
 */
class PipelineSpockTestBase extends Specification  implements RegressionTest {

    /**
     * Delegate to the test helper
     */
    @Delegate PipelineTestHelper pipelineTestHelper

    def target = [:]
    def dictionary = [:]
    def delivery = [:]
    Bundle bundle

    String matrice = "fr.pe.tech:declarative-pipeline-matrice:tic"
    String mailto = "prenom.nom@pole-emploi.fr"
    String projectName = "PipelineSharedLib"
    String RESULT_SUCCESS = "SUCCESS"
    String DEFAULT_JOB_URL = "http://jenkins.intra/myjob"
    String CADRE_TIC = 'TIC'

    def ajouterVersionStable
    def analyserQualite
    def construireAngularVersionStable
    def construireVersionStable
    def construireVersionStableGitMvn
    def construireVersionStableRtcMvn
    def demanderLivraison
    def deployer
    def livrer
    def notifier
    def promouvoir
    def redemarrer
    def sauvegarderMatrice
    def verifierDeploiement
    def verifierQualite

    /**
     * Do the common setup
     */
    def setup() {

        // Set callstacks path for RegressionTest
        // callStackPath = 'pipelineTests/groovy/tests/callstacks/'

        // Create and config the helper
        pipelineTestHelper = new PipelineTestHelper()
        pipelineTestHelper.setUp()

        target.type =  "wi"
        target.host = "slzks7"
        target.owner = "wlsas261"

        dictionary["fadia.cadre"] = "a"
        dictionary["fadia.appli"] = "slipe"
        dictionary["fadia.domaine"] = "s"
        dictionary["fadia.codeEnveloppe"] = "ty"
        dictionary["ic.deploy.type"] = "osb"

        bundle = BundleFactory.createSimpleBundle("fr.pe.tech", "pipeline-shared-lib", "1.0.0", "jar")
        bundle.changelog = 'Changelog...'

        delivery.approUrl = "http://proven.intra/approUrl"
        delivery.logUrl = "http://proven.intra/logUrl"
        delivery.status = RESULT_SUCCESS

        ajouterVersionStable = loadScript('vars/ajouterVersionStable.groovy')
        analyserQualite = loadScript('vars/analyserQualite.groovy')
        construireVersionStable = loadScript('vars/construireVersionStable.groovy')
        construireVersionStableGitMvn = loadScript('vars/construireVersionStableGitMvn.groovy')
        construireVersionStableRtcMvn = loadScript('vars/construireVersionStableRtcMvn.groovy')
        demanderLivraison = loadScript('vars/demanderLivraison.groovy')
        deployer = loadScript('vars/deployer.groovy')
        livrer = loadScript('vars/livrer.groovy')
        notifier = loadScript('vars/notifier.groovy')
        promouvoir = loadScript('vars/promouvoir.groovy')
        redemarrer = loadScript('vars/redemarrer.groovy')
        sauvegarderMatrice = loadScript('vars/sauvegarderMatrice.groovy')
        verifierDeploiement = loadScript('vars/verifierDeploiement.groovy')
        verifierQualite = loadScript('vars/verifierQualite.groovy')
    }
}