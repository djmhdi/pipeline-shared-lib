package com.capgemini.fs.jenkins.plugins.notification

import spock.lang.*
import com.capgemini.fs.jenkins.plugins.notification.*
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
class BundleFactorySpec extends PipelineSpockTestBase {
    String bundleGAV
    Bundle bundle
    String groupId,artifactId,version,qualifier,packaging
    def setup() {

    }

    def cleanup() { target.clear() }

    def "La fabrique d'une instance de bundle pour une release standard incrémente bien le bugfix et respecte le format approprié"() {
        given: "Le bundle snapshot avec les caractéristiques suivantes"
            groupId = "fr.pe.test"
            artifactId = "bundle-test"
            version = "1.0.0-SNAPSHOT"
            qualifier = "12"
            packaging = "jar"
        when: "On fabrique une instance de bundle de type release standard"
            bundle = BundleFactory.createStandardReleaseBundle(groupId, artifactId, version, qualifier, packaging)
        then: "La version release est correctement formatée"
            Assertions.assertThat(bundle.releaseVersion).isEqualTo("fr.pe.test:bundle-test:1.0.0-12")
        then: "Le job s'exécute correctement"
            Assertions.assertThat(bundle.nextDevelopmentVersion).isEqualTo("fr.pe.test:bundle-test:1.0.1-SNAPSHOT")
        then: "Par défaut, la propriété unzipArtifact est à false"
            Assertions.assertThat(bundle.unzipArtifact).isEqualTo(false)
    }
}
