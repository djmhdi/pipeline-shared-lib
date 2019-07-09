#!/usr/bin/groovy
import com.capgemini.fs.jenkins.plugins.util.JenkinsNodes
import com.capgemini.fs.jenkins.plugins.proven.ProvenResource
import com.capgemini.fs.jenkins.plugins.restart.Restart

def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    check(config)

    echo "Redémarrage lancé pour la matrice  :" + config.matrice
    def pvnMatrix = proven.getMatrix {
        gav = config.matrice
    }
    echo "Matrix                : ${pvnMatrix.toString()} "
    String[] bundles = config.bundles

    for (int i = 0; i < bundles.length; i++) {
        def bundleGAV = bundles[i]
        echo "Redémarrage des targets du Bundle             : ${bundleGAV}"
        restartTargetsBundle(pvnMatrix, bundleGAV)
    }
}

def restartTargetsBundle(pvnMatrix, bundleGAV) {

    echo "Recherche du gav du dictionnaire pour le bundle : ${bundleGAV}"
    String dictionaryGAV = ProvenResource.dictionaryGavFor(bundleGAV, pvnMatrix)

    echo "Recherche du dictionnaire pour le GAV : ${dictionaryGAV}"
    def dictionary = proven.getDictionary {
        gav = "${dictionaryGAV}"
    }

    def targetDescriptors = ProvenResource.targetsDescriptorFor(bundleGAV, pvnMatrix)
    echo "targetDescriptors : $targetDescriptors"

    for (String targetDescriptor : targetDescriptors) {
        Restart.restart(this, targetDescriptor, dictionary)
    }
}


def check(config) {
    if (!config.bundles) {
        throw new Exception("le parametre 'bundles' est obligatoire !")
    }
}