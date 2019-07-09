#!/usr/bin/groovy
import com.capgemini.fs.jenkins.plugins.notification.NotificationDataKeys
import com.capgemini.fs.jenkins.plugins.util.HtmlTemplates
import com.capgemini.fs.jenkins.plugins.util.*

/**
 * bundle : le bundle qui contient la version release et la future version de développement ex : releaseBundle
 * bundleRelativePath : chemin du livrable à intégrer
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

    try {
        echo "\n============== Build Release ========================="

        // Construction de la commande mvn deploy
        String mvnCmd = "mvn -gs ${env['MAVEN_PE_SETTINGS']} deploy:deploy-file "
        mvnCmd <<= "-Durl=${env[config.repository.envParameterName]} "

        //      mvnCmd <<= "-Dfile=${nomLivrableNormalise} "
        mvnCmd <<= "-Dfile=${config.bundleRelativePath} "
        mvnCmd <<= "-Dpackaging=${config.bundle.packaging} "
        mvnCmd <<= "-DgroupId=${config.bundle.groupId} "
        mvnCmd <<= "-DartifactId=${config.bundle.artifactId} "
        mvnCmd <<= "-Dversion=${config.bundle.releaseVersion} "

        // Exécution de la commande mvn deploy
        sh "${mvnCmd}"

        creerBundleProven {
            bundles = [config.bundle]
            repository = config.repository
        }

        currentBuild.result = 'SUCCESS'
    } catch (exception) {
        currentBuild.result = 'FAILURE'
        throw exception
    } finally {
        // On boucle sur les notifications et on y ajoute le resultat du deliver proven
        for (int i = 0; i < config.notifications.size() ; i++) {
             config.notifications[i].appendData(NotificationDataKeys.BUNDLE.key, config.bundle)
        }

        notifier {
             notifications = config.notifications
         }
    }
}

def check(def config) {
    if (!config.bundleRelativePath) {
        throw new Exception("le parametre 'bundleRelativePath' est obligatoire !")
    }

    if (!config.bundle) {
        throw new Exception("le parametre bundle est obligatoire !")
    }

    if (!config.repository) {
        config.repository = RepositoryEnum.ALPHA
    }

    if (!config.notifications) {
        config.notifications = []
    }
}
