#!/usr/bin/groovy
import com.capgemini.fs.jenkins.plugins.notification.NotificationDataKeys
import com.capgemini.fs.jenkins.plugins.util.HtmlTemplates
import static com.capgemini.fs.jenkins.plugins.mail.HtmlMailFactory.*
import com.capgemini.fs.jenkins.plugins.mail.MailDefinition
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

    if (env.NODE_NAME) {
        echo "\nUtilisation du noeud  d'execution courant : [" + env.NODE_NAME + "]."
        createAngularRelease(config)
    } else {
        echo "\nAllocation d'un nouveau noeud d'execution."
        if (!config.node) {
            config.node = JenkinsNodes.NODEJS.label
        }
        node(config.node) {
            checkout scm
            createAngularRelease(config)
        }
    }
}

def createAngularRelease(config) {
    echo "\n============== MESURE D'IMPACT dev mainline ========================="


        withEnv([
				"PATH+GROOVY=${tool 'GROOVY_2_4_3_HOME'}",
				"PATH+NODE=${tool env.NODEJS_VERSION_HOME}",
                "PATH+JAVA=${tool env.JAVA_VERSION_HOME}/bin",
                "PATH+MAVEN=${tool env.MAVEN_VERSION_HOME}/bin"
        ]) {
            try {
                echo "\n============== Build Release ========================="
                // TODO release avec tag et montée de version
                //sh "npm run-script release"
                
                // TODO temporaire en attendant la commande ci-dessus (angular 4.0)
                def releaseVersion = config.bundle.releaseVersion
                echo "\n=== Release de la version ==="
                sh "npm version ${releaseVersion} --no-git-tag-version"
                echo "\n=== Installation des dépendances ==="
				sh "npm install"
				sh "npm list --depth=0"
                echo "\n=== Construction du livrable ==="
                sh "npm run-script build"
				sh "npm run-script deploy"
				sh "git commit -am \"[Jenkins] Release de la version ${releaseVersion}\" && git tag -a ${releaseVersion} -m \"version ${releaseVersion}\" && git push --tags"
			    
			     // Création du bundle dans Proven
                construireVersionStable.createBundle(config.bundle, true, config.repository)
			    
			    echo "\n=== Passage à la version de dev suivante ==="
			    def devVersion = config.bundle.nextDevelopmentVersion
			    sh "npm version ${devVersion} --no-git-tag-version"
			    sh "git commit -am \"[Jenkins] passage a la prochaine version ${devVersion}\" && git push"

                currentBuild.result = 'SUCCESS'
            } catch (exception) {
                currentBuild.result = 'FAILURE'
                throw exception
            } finally {
                currentBuild.description = "${config.bundle.releaseVersion}"
                // On boucle sur les notifications et on y ajoute le resultat du deliver proven
                for (int i = 0; i < config.notifications.size() ; i++) {
                    config.notifications[i].appendData(NotificationDataKeys.BUNDLE.key, config.bundle)
                }

                notifier {
                    notifications = config.notifications
                }
            }
        }
}

def check(def config) {
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
