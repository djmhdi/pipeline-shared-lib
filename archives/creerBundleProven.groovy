#!/usr/bin/groovy
import com.capgemini.fs.jenkins.plugins.notification.NotificationDataKeys
import com.capgemini.fs.jenkins.plugins.util.HtmlTemplates
import com.capgemini.fs.jenkins.plugins.util.*

/**
 * parentPom : le POM sur lequel va s'exécuter la construction ex: pom.xml
 * bundle : le bundle qui contient la version release et la future version de développement ex : releaseBundle
 * mavenProfiles : liste de profiles pour la construction Maven ex : "livraison-proven,repo-alpha"
 * emailTo : liste d'adresses email à qui envoyer le résultat de la release "john.doe@pole-emploi.fr, smith.smith@pole-emploi.fr"
 * repository : nom du dépôt dans lequelle stocker la release ex : RepositoryEnum.ALPHA
 * notifications : envoyer un notification sur résultat positif ex : false
 */
def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()
    check(config)

    String artifactURL
    for (def bundle in config.bundles) {
        artifactURL = bundle.getReleaseArchiveURL(env["ARTIFACTORY_URL"], config.promotion.repositoryKey)
        echo "\n============== Create Proven Bundle ========================="
        echo "==ArtifactURL : ${artifactURL}"

        proven.createOrUpdateBundle {
            groupId = bundle.groupId
            artifactId = bundle.artifactId
            version = bundle.releaseVersion
            url = artifactURL
            promotionLevel = "${config.promotion.repositoryKey}"
            unzip = bundle.unzipArtifact
        }

        publierBuildInfo {
            bundles = config.bundles
            currentPromotion = config.promotion
            artifactoryServer = config.artifactoryServer
        }

        promouvoir {
            bundles = config.bundles
            promotion = config.promotion
            artifactoryServer = config.artifactoryServer
        }
    }
}

def check(def config) {
    if (!config.bundles) {
        throw new Exception("le parametre bundle est obligatoire !")
    }

    if (!config.promotion) {
        config.promotion = PromotionStatus.QUALIFICATION_COMPOSANT
    }
}