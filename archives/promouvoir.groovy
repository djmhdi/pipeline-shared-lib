#!/usr/bin/groovy
import com.capgemini.fs.jenkins.plugins.notification.NotificationDataKeys
import com.capgemini.fs.jenkins.plugins.bundle.BundleFactory
import com.capgemini.fs.jenkins.plugins.bundle.Bundle
import com.capgemini.fs.jenkins.plugins.util.*

def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    check(config)

    def server = Artifactory.server config.artifactoryServer
    server.credentialsId = config.artifactoryUser

    def promotionConfig = []
    String artifactUrlDest
    for (Bundle bundleToPromote : config.bundles) {
        artifactUrlDest = bundleToPromote.getReleaseArchiveURL(env["ARTIFACTORY_URL"], config.promotion.repositoryKey)
        proven.promoteBundle {
            bundle = bundleToPromote.releaseGAV
            url = artifactUrlDest
            promotionLevel = config.promotion.name()
        }

        promotionConfig = [
                //Mandatory parameters
                'buildName'          : bundleToPromote.artifactId,
                'buildNumber'        : bundleToPromote.releaseVersion,
                'targetRepo'         : config.promotion.repositoryKey,

                //Optional parameters
                'comment'            : config.promotion.description,
                'sourceRepo'         : config.currentPromotion.repositoryKey,
                'status'             : config.promotion.name(),
                'includeDependencies': true,
                'failFast'           : true,
                'copy'               : config.copyArtifact
        ]

        server.promote promotionConfig
    }

    // Mises à jour des matrices avec les versions de bundles nouvellement promues
    if (config.promotion != PromotionStatus.ABANDON) {
        updateMatrices(config.bundles, config.matrices)
    }

    // On boucle sur les notifications et on y ajoute le resultat du deliver proven
    for (int i = 0; i < config.notifications.size(); i++) {
        config.notifications[i].appendData(NotificationDataKeys.BUNDLES.key, config.bundles)
        config.notifications[i].appendData(NotificationDataKeys.PROMOTION_LEVEL.key, config.promotion.name())
    }

    notifier {
        notifications = config.notifications
    }
}

def check(def config) {
    if (!config.bundles) {
        throw new Exception("le parametre 'bundles' est obligatoire ! ")
    }

    if (!config.promotion) {
        throw new Exception("Le parametre 'promotion' est obligatoire et prend pour valeur une instance de l'énumération com.capgemini.fs.jenkins.plugins.util.plugins.PromotionStatus.")
    }

    // Identifiant de la configuration du serveur artifactory dans Administration Jenkins
    if (!config.artifactoryServer) {
        throw new Exception("Le parametre 'artifactoryServer' est obligatoire !")
    }

    if (!config.currentPromotion) {
        config.currentPromotion = config.promotion
        config.copyArtifact = false
    } else {
        config.copyArtifact = true
    }

    // Credential pour l'utilisateur artifactory
    if (!config.artifactoryUser) {
        config.artifactoryUser = "artifactory_user"
    }

    if (!config.matrices) {
        config.matrices = []
    }

    if (!config.notifications) {
        config.notifications = []
    }
}

def updateMatrices(def bundles, def matrices) {
    def bundlesGAV = BundleFactory.createListGAVFromBundle(bundles)
    for (def matrice in matrices) {
        echo "\n============== Update matrix: ${matrice} ========================="
        echo " -- newBundlesGAV : $bundlesGAV"
        proven.deltaUpdateBundleVersionInMatrix {
            gav = matrice
            deltas = bundlesGAV
        }
        echo "============================================================================="
    }
}

/*def server = Artifactory.server config.artifactoryServer
   server.credentialsId = config.artifactoryUser

   def downloadSpec = """{
                        "files": [
                         {
                             "pattern": "${config.promotion.repositoryKey}/${bundle.deploymentPath}",
                             "target": "download/${bundle.artifactId}"
                           }
                        ]
                       }"""

   def uploadSpec = """{
                         "files": [
                           {
                             "pattern": "download/${bundle.artifactId}/*.${bundle.packaging}",
                             "target": "${config.promotion.repositoryKey}/${bundle.deploymentPath}"
                           }
                        ]
                       }"""

   def buildInfo = Artifactory.newBuildInfo()
   buildInfo.name = 'ds013-pfe-batch'
   buildInfo.number = '1.2.13-23'

   server.download spec: downloadSpec, buildInfo: buildInfo
   server.upload spec: uploadSpec, buildInfo: buildInfo

   server.publishBuildInfo buildInfo */