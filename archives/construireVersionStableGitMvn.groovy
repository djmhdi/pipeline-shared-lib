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

    createRelease(config)
}

def check(def config) {
    if (!config.bundle) {
        throw new Exception("le parametre 'bundle' est obligatoire !")
    }

    if (!config.parentPom) {
        throw new Exception("le parametre 'parentPom' est obligatoire !")
    }

    if (!config.bundlePom) {
        throw new Exception("le parametre 'bundlePom' est obligatoire !")
    }

    if (!config.bundleRelativePath) {
        throw new Exception("le parametre 'bundleRelativePath' est obligatoire !")
    }

    if (!config.mavenGoals) {
        throw new Exception("le parametre 'mavenGoals' est obligatoire !")
    }

    if (!config.repository) {
        config.repository = RepositoryEnum.ALPHA
    }

    if (!config.notifications) {
        config.notifications = []
    }
}

def createRelease(def config) {
    try {
        echo "\n============== Build Release ========================="
        def branch = sh returnStdout: true, script: 'git branch | grep \\* | cut -d \' \' -f2-'
        if (branch == "(no branch)")
            throw new IllegalArgumentException("(no branch)")

        String mvnReleaseVersion = "mvn -gs ${env['MAVEN_PE_SETTINGS']} -f ${config.parentPom} versions:use-releases versions:set -DnewVersion=${config.bundle.releaseVersion}"

        String mvnBuild = "mvn -f ${config.parentPom} -gs ${env['MAVEN_PE_SETTINGS']} ${config.mavenGoals} "
        if (config.mavenProfiles) {
            mvnBuild <<= "-P${config.mavenProfiles} "
        }
        if (config.skipTests) {
            mvnBuild <<= "-Dmaven.test.skip=${config.skipTests} "
        }
        if (config.mavenOptions) {
            mvnBuild <<= "${config.mavenOptions} "
        }
        mvnBuild <<= "-Djava.io.tmpdir=${env['JENKINS_TMP_DIR']} "

        String mvnDeployFile = "mvn -gs ${env['MAVEN_PE_SETTINGS']} "
        mvnDeployFile <<= "deploy:deploy-file -Durl=${env[config.repository.envParameterName]} -DpomFile=${config.bundlePom} -Dfile=${config.bundleRelativePath} -Dpackaging=${config.bundle.packaging}"

        String mvnNextDevVersion = "mvn -gs ${env['MAVEN_PE_SETTINGS']} -f ${config.parentPom} versions:set -DnewVersion=${config.bundle.nextDevelopmentVersion}"

        sh "${mvnReleaseVersion}"
        sh "${mvnBuild}"
        sh 'find . -name "pom.xml" | xargs git add'
        sh "git tag -a ${config.bundle.artifactId}-${config.bundle.releaseVersion} -m \"Nouvelle version release ${config.bundle.releaseVersion}\""
        sh "git commit -m \"release version ${config.bundle.artifactId}-${config.bundle.releaseVersion}\""
        sh "git push -u origin ${branch}"

        sh "${mvnDeployFile}"

        try {
            sh "${mvnNextDevVersion}"
            sh 'find . -name "pom.xml" | xargs git add'
            sh "git commit -m \"next dev version ${config.bundle.artifactId}-${config.bundle.nextDevelopmentVersion}\""
            sh "git push -u origin ${branch}"
        } catch (exception) {
            echo "Warning! Problème lors de l'incrémentation vers la version SNAPSHOT !"
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
