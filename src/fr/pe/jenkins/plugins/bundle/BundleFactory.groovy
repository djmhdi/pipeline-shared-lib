#!/usr/bin/groovy
package fr.pe.jenkins.plugins.bundle

import java.util.logging.Logger
import fr.pe.jenkins.plugins.bundle.Bundle

class BundleFactory {

    static Logger logger = Logger.getLogger('fr.pe.jenkins.plugins.bundle.BundleFactory')

    /**
     * Initialise un bundle à partir de la version courante et d'un qualifier
     * Cette méthode utilitaire gère les versions sur deux digits ex: 1.0-SNAPSHOT
     * la version release est calculé à partir de la version courant, à laquelle on remplace "SNAPSHOT" par le qualifier
     * l'indice du bugfix est incremente de 1 pour la prochaine version de developpement
     */
    static Bundle createStandardReleaseBundle(String groupId, String artifactId, String currentSnapshotVersion, String qualifier, String packaging) {
        List<String> versionQualifierSplitted = currentSnapshotVersion.split("-")
        List<String> versionSplitted = versionQualifierSplitted[0].split("\\.")
        String versionMajeur = versionSplitted[0] ? versionSplitted[0] : "0"
        String versionMineur = versionSplitted[1] ? versionSplitted[1] : "0"

        String releaseVersion = versionQualifierSplitted[0]
        if (qualifier) {
            releaseVersion += "-${qualifier}"
        }

        String nextDevelopmentVersion = ""
        if (versionSplitted.size() > 2) {
            String bugFix = versionSplitted[2] ? versionSplitted[2] : "0"
            nextDevelopmentVersion = "${versionMajeur}.${versionMineur}.${bugFix.toInteger() + 1}-SNAPSHOT"
        } else {
            nextDevelopmentVersion = "${versionMajeur}.${versionMineur}-SNAPSHOT"
        }

        return new Bundle(groupId, artifactId, releaseVersion, nextDevelopmentVersion, packaging)
    }

    /**
     * Methode pour instancier un nouveau bundle release standard a partir d un fichier pom en forcant le packaging
     */
    static Bundle createStandardReleaseBundleFromPom(def pomDefinition, def qualifier, def packaging) {
        def groupId = pomDefinition.parent ? pomDefinition.parent.groupId : pomDefinition.groupId
        def artifactId = pomDefinition.artifactId
        def version = pomDefinition.parent ? pomDefinition.parent.version : pomDefinition.version

        return createStandardReleaseBundle(groupId, artifactId, version, qualifier, packaging)
    }

    /**
     * Methode pour instancier un nouveau bundle release standard a partir d un fichier pom
     */
    static Bundle createStandardReleaseBundleFromPom(def pomDefinition, def qualifier) {
        return createStandardReleaseBundleFromPom(pomDefinition, qualifier, pomDefinition.packaging)
    }

    static Bundle createStandardReleaseBundleFromPomWithTimestampQualifier(def pomDefinition) {
        String timestampQualifier = new Date().format('MMddHHmmss')

        return createStandardReleaseBundleFromPom(pomDefinition, timestampQualifier, pomDefinition.packaging)
    }

    /**
     * Methode pour instancier un nouveau bundle a partir d un bundle existant, la nextDevelopmentVersion et la releaseVersion sont equal à la currentVersion
     */
    static Bundle createSimpleBundle(String groupId, String artifactId, String currentVersion, String packaging) {
        return new Bundle(groupId, artifactId, currentVersion, currentVersion, packaging)
    }

    /**
     * Methode pour instancier un nouveau bundle a partir d un GAV, la nextDevelopmentVersion est egale à la releaseVersion
     */
    static Bundle createBundleFromGAV(def gav) {
        def gavSplitted = gav.split(':')
        String extension = ""
        if (gavSplitted.size() == 4) {
            extension = gavSplitted[3]
        }
        return new Bundle(gavSplitted[0], gavSplitted[1], gavSplitted[2], gavSplitted[2], extension)
    }

    static Bundle createBundleFromGAV(def gav, String packaging) {
        def gavSplitted = gav.split(':')

        return new Bundle(gavSplitted[0], gavSplitted[1], gavSplitted[2], gavSplitted[2], packaging)
    }

    /**
     * Methode pour instancier une liste de bundle a partir d une liste de GAV
     */
    static Bundle createBundlesFromListGAV(def listGav) {
        def bundles = []
        for (def gav in listGav) {
            bundles.add(createBundleFromGAV(gav))
        }
        return bundles
    }

    /**
     * Methode pour instancier une liste de GAV (String) a partir d une liste de bundle
     */
    static String[] createListGAVFromBundle(def bundles) {
        def bundlesGAV = []
        for (def bundle in bundles) {
            bundlesGAV.add(bundle.releaseGAV)
        }
        return bundlesGAV
    }

    /**
     * Methode pour instancier un nouveau bundle simple a partir d un fichier pom
     */
    static Bundle createStandardSimpleBundleFromPom(def pomDefinition) {
        def groupId = pomDefinition.parent ? pomDefinition.parent.groupId : pomDefinition.groupId
        def artifactId = pomDefinition.artifactId
        def version = pomDefinition.parent ? pomDefinition.parent.version : pomDefinition.version
        def packaging = pomDefinition.packaging

        return createSimpleBundle(groupId, artifactId, version, packaging)
    }
}