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

    //--body
    println "-- config : " + config
    
    if (env.NODE_NAME) {
        echo "\nUtilisation du noeud  d'execution courant : [" + env.NODE_NAME + "]."
        createAngularSnapshot(config)
    } else {
        echo "\nAllocation d'un nouveau noeud d'execution."
        if (!config.node) {
            config.node = JenkinsNodes.NODEJS.label
        }
        node(config.node) {
            checkout scm
            createAngularSnapshot(config)
        }
    }
}

def createAngularSnapshot(def config) {
        withEnv([
                "PATH+GROOVY=${tool env.GROOVY_VERSION_HOME}",
                "PATH+NODE=${tool env.NODEJS_VERSION_HOME}/bin",
                "PATH+JAVA=${tool env.JAVA_VERSION_HOME}/bin",
                "PATH+MAVEN=${tool env.MAVEN_VERSION_HOME}/bin"
        ]) {
            try {
                echo "\n============== Build Angular Snapshot ========================="

                echo "\n=== Installation des dépendances ==="
                if (config.clean) {
                    sh "npm prune"
                }
				sh "npm install"
				sh "npm list --depth=0"
				
				if (!config.skipTests) {
				    echo "\n=== Tests unitaires ==="
					sh "npm test"
					if (!fileExists("${pwd()}/node_modules/sonar-scanner")) {
				        echo 'node module sonar-scanner doit être installer'
				        currentBuild.result = 'UNSTABLE'
				    } else {
				        if (!config.branch) {
				            if (!env.BRANCH_NAME) {
                                env.BRANCH_NAME='DEV'
                            }
                            config.branch = "${en['BRANCH_NAME']}"
                        }
				        try {
				            withSonarQubeEnv(config.sonar) {
				                writeFile (file: "./node_modules/sonar-scanner/conf/sonar-scanner.properties", text: "sonar.projectKey=${snapshotBundle.artifactId}\nsonar.projectName=${projectName}\nsonar.projectVersion=${snapshotBundle.releaseVersion}\n" 
				                    +"sonar.sources=.\nsonar.sourceEncoding=UTF-8\nsonar.exclusions=node_modules/**/*,test/**/*,gulpfile.js,pipeline/*,reports/**/*,dist/**/*\n"
				                    +"sonar.branch=${config.branch}\n"
				                    +"sonar.javascript.lcov.reportPaths=reports/coverage/lcov.info\n")
				                sh "npm run sonar-scanner"
				            }
				        } catch(err) {
				            currentBuild.result = 'UNSTABLE'
				        }
				    }
				    junit testResults: 'reports/tests/TESTS-*.xml'
				}
				
				echo "\n=== Création du livrable ==="
				sh "npm run-script build"
				sh "npm run-script deploy"

                // Création du bundle dans Proven
                construireVersionStable.createBundle(config.bundle, true, config.repository)

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
}

def check(def config) {
    if (!config.bundle) {
        throw new Exception("le parametre bundle est obligatoire !")
    }
    if (!config.repository) {
        config.repository = RepositoryEnum.POLEEMPLOI
    }
    if (!config.sonar) {
        config.sonar = 'sonar001'
    }

    if (!config.notifications) {
        config.notifications = []
    }
}
