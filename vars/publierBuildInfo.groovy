#!/usr/bin/groovy
import fr.pe.jenkins.plugins.notification.NotificationDataKeys
import fr.pe.jenkins.plugins.bundle.BundleFactory
import fr.pe.jenkins.plugins.util.*
import org.jfrog.build.client.DeployDetails

def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    check(config)

    def server = Artifactory.server config.artifactoryServer
    server.credentialsId = config.artifactoryUser

    def buildInfo, downloadSpec, uploadSpec
    for (def bundle : config.bundles) {
        buildInfo = Artifactory.newBuildInfo()
        buildInfo.name = bundle.artifactId
        buildInfo.number = bundle.releaseVersion

        downloadSpec = getDownloadSpec(bundle, config.currentPromotion, config.localTarget)
        uploadSpec = getUploadSpec(bundle, config.currentPromotion, config.localTarget)

        server.download spec: downloadSpec, buildInfo: buildInfo
        server.upload spec: uploadSpec, buildInfo: buildInfo

        server.publishBuildInfo buildInfo
    }
}

def getDownloadSpec(def bundle, PromotionStatus currentPromotion, def localTarget) {
    return """{
                "files": [{
                             "pattern": "${currentPromotion.repositoryKey}/${bundle.deploymentPath}",
                             "target": "${localTarget}"
                       }]
                 }"""
}

def getUploadSpec(def bundle, PromotionStatus currentPromotion, def localTarget) {
    return """{
                "files": [{
                        "pattern": "${localTarget}/*/${bundle.artifactName}",
                        "target": "${currentPromotion.repositoryKey}/${bundle.deploymentPath}"
                    }]
                 }"""
}

def check(def config) {
    if (!config.bundles) {
        throw new Exception("le parametre 'bundles' est obligatoire ! ")
    }

    if (!config.currentPromotion) {
        throw new Exception("Le parametre 'currentPromotion' est obligatoire et prend pour valeur une instance de l'énumération fr.pe.jenkins.plugins.util.plugins.PromotionStatus.")
    }

    // Identifiant de la configuration du serveur artifactory dans Administration Jenkins
    if (!config.artifactoryServer) {
        throw new Exception("Le parametre 'artifactoryServer' est obligatoire !")
    }

    // Credential pour l'utilisateur artifactory
    if (!config.artifactoryUser) {
        config.artifactoryUser = "artifactory_user"
    }

    if (!config.localTarget) {
        config.localTarget = "target"
    }
}
