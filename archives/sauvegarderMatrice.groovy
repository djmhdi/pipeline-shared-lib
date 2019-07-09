#!/usr/bin/groovy
import com.capgemini.fs.jenkins.plugins.util.JenkinsNodes
import com.capgemini.fs.jenkins.plugins.bundle.BundleFactory

def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    check(config)

    if (config.bundles) {
        String[] bundlesGAV = BundleFactory.createListGAVFromBundle(config.bundles)
        backupMatrix(config.matrice, bundlesGAV, config.prefix, config.suffix)
    }
}

def check(def config) {
    if (!config.matrice) {
        throw new Exception("le parametre 'matrice' est obligatoire !")
    }
    if (!config.suffix) {
        config.suffix = "_backup-" + new Date().format('yyyyMMdd')
    }
    if (!config.prefix) {
        config.prefix = "_"
    }
}

def backupMatrix(String matrixGAV, String[] bundlesGAV, String prefix, String suffix) {
    echo "\n============== backup matrix ${matrixGAV} ========================="
    String matrixBackupGav = getBackupGAV(matrixGAV, prefix, suffix)

    def matrix = proven.getMatrix {
        gav = "${matrixGAV}"
    }

    for (def element in matrix.elements) {
        if (bundlesGAV == null || bundlesGAV.contains(element.bundleGAV)) {
            backupDictionary(element.dictionaryGAV, prefix, suffix)
        }
    }

    echo " -- backup matrix ${matrix.gav} in $matrixBackupGav"
    matrix.gav = matrixBackupGav
    def newMatrix = proven.matrixCreateOrUpdate {
        json = "${matrix.json}"
    }
    return newMatrix
}

String backupDictionary(String dictionaryGAV, String prefix, String suffix) {
    String dicoBackupGAV = getBackupGAV(dictionaryGAV, prefix, suffix)
    echo " -- backup dictionary $dictionaryGAV in ${dicoBackupGAV}"
    def valoDico = proven.getDictionary {
        gav = "$dictionaryGAV"
    }
    proven.createDictionary {
        gav = "$dicoBackupGAV"
        valorisation = valoDico
    }
    return dicoBackupGAV
}

String getBackupGAV(String gav, String prefix, String suffix) {
    List<String> gavSplitted = gav.split(":")
    String groupId = gavSplitted[0]
    String artifactId = gavSplitted[1]
    String version = gavSplitted[2]
    String backupVersion = "${prefix}${version}${suffix}"

    return "${groupId}:${artifactId}:${backupVersion}"
}