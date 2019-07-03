package fr.pe.jenkins.plugins.deploy.command

import spock.lang.*
import fr.pe.jenkins.plugins.deploy.command.DeployTY
import fr.pe.jenkins.plugins.bundle.Bundle

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
class DeployTYSpec extends Specification {
    def target = [:]
    def dictionary = [:]
    Bundle bundle

    def setup() {
        target.type =  "wi"
        target.host = "slzks7"
        target.owner = "wlsas261"
        target.numInstance = "261"

        dictionary["fadia.cadre"] = "a"
        dictionary["fadia.appli"] = "slipe"
        dictionary["fadia.domaine"] = "s"
        dictionary["fadia.codeEnveloppe"] = "ty"
        dictionary["ic.deploy.type"] = "osb"

        bundle = new Bundle("fr.pe.tech.test", "jenkins-pipeline-test", "1.0.1", "1.0.2-SNAPSHOT", "ear")
    }

    def cleanup() { target.clear() }

    def "fadia.nomArtefact null or containing underscore are installed with INSTALL_EAR"() {
        DeployTY ty = new DeployTY()

        when:
            String cmdWithArtefactNull = ty.execute(null, bundle, target , dictionary, '')

            dictionary["fadia.nomArtefact"] = "jenkins-pipeline-test_1.0.1.ear"
            String cmdWithArtefactNotNullWithUnderscore = ty.execute(null, bundle, target , dictionary, '')

        then:
            cmdWithArtefactNull.contains("fadia.z.install_ear.ksh slipe 261 dmety slipe")

        then:
            cmdWithArtefactNotNullWithUnderscore.contains("fadia.z.install_ear.ksh slipe 261 dmety slipe")
    }

    def "fadia.nomArtefact not null are installed with INSTALL_COPIE_EAR"() {
        dictionary["fadia.nomArtefact"] = "jenkins-pipeline-test.ear"

        DeployTY ty = new DeployTY()

        when:
        String cmdWithArtefactNotNullWithNoUnderscore = ty.execute(null, bundle, target , dictionary, '')

        then:
        cmdWithArtefactNotNullWithNoUnderscore.contains("fadia.z.install_copie_ear.ksh slipe slipe dmety 261")
    }
}
