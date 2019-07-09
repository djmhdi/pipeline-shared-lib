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

    try {
        echo "\n============== Build Release ========================="

        // Construction de la commande mvn release perform
        String mvnCmd = "mvn -gs ${env['MAVEN_PE_SETTINGS']} -f ${config.parentPom} --batch-mode release:prepare release:perform "
        if (config.mavenProfiles) {
            mvnCmd <<= "-P${config.mavenProfiles} "
        }

        mvnCmd <<= "-Darguments='"
        if (config.skipTests) {
            mvnCmd <<= "-Dmaven.test.skip=${config.skipTests} "
        }
        if (config.mavenOptions) {
            mvnCmd <<= "${config.mavenOptions} "
        }
        mvnCmd <<= "-Djava.io.tmpdir=${env['JENKINS_TMP_DIR']} "
        mvnCmd <<= "-DaltDeploymentRepository=${config.repository.credentialId}::default::${env[config.repository.envParameterName]}' "

        mvnCmd <<= "-DuseEditMode=true -Dresume=false "
        mvnCmd <<= "-DreleaseVersion=${config.bundle.releaseVersion} -DdevelopmentVersion=${config.bundle.nextDevelopmentVersion}"

        // Exécution de la commande mvn release perform
        sh "${mvnCmd}"

        currentBuild.result = 'SUCCESS'
    } catch (exception) {
        currentBuild.result = 'FAILURE'
        throw exception
    } finally {
        // On boucle sur les notifications et on y ajoute le resultat du deliver proven
        for (int i = 0; i < config.notifications.size(); i++) {
            config.notifications[i].appendData(NotificationDataKeys.BUNDLE.key, config.bundle)
        }

        notifier {
            notifications = config.notifications
        }
    }
}

def check(def config) {
    if (!config.parentPom) {
        throw new Exception("le parametre 'parentPom' est obligatoire !")
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
