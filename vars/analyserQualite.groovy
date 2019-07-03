#!/usr/bin/groovy
import fr.pe.jenkins.plugins.util.*
import fr.pe.jenkins.plugins.quality.*

def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()
    check(config)

    analyserSonarQualite(config)
}

def check(def config) {
    if (!config.sonar) {
        throw new Exception("le parametre 'sonar' est obligatoire !")
    }

    if (!env.BRANCH_NAME) {
        env.BRANCH_NAME = 'DEV'
    }

    if (!config.pom) {
        config.pom = "pom.xml"
    }

    if (!config.branch) {
        config.branch = "${env['BRANCH_NAME']}"
    }
}

def analyserSonarQualite(def config) {

    withSonarQubeEnv(config.sonar) {
        String sonarOpts = "-Dsonar.host.url=${env['SONAR_HOST_URL']} -Dsonar.branch=${config.branch} "
        // la ligne ci-dessous est pour les Sonar en version inférieure à la 5.1
        sonarOpts <<= "-Dsonar.jdbc.url=${env['SONAR_JDBC_URL']} -Dsonar.jdbc.username=${env['SONAR_JDBC_USERNAME']} -Dsonar.jdbc.password=${env['SONAR_JDBC_PASSWORD']} "

            // Ne marche pas en Sonar 6.4, doit être fait manuellement dans Sonar pour l'instant
            // voir le lien : https://docs.sonarqube.org/display/SONAR/Webhooks
            // ou il est dit "There is no Web API to register/unregister a Webhook. You may only use the UI to perform this action."
            // En espérant que ce la marche dans une version ultérieure
            // On garde le code car ce n'est pas la manière la plus évidente de passer des paramètres
//            String webhooksUrl = (env.JENKINS_URL?.startsWith('http')) ? env.JENKINS_URL : 'http://' + env.JENKINS_URL
//            sonarOpts <<= "-Dsonar.webhooks.project=1 "
//            sonarOpts <<= "-Dsonar.webhooks.project.1.name=Jenkins "
//            sonarOpts <<= "-Dsonar.webhooks.project.1.url=${webhooksUrl}/sonarqube-webhook/ "

            if (config.sources) {
                sonarOpts <<= "-Dsonar.sources=${config.sources} "
            }

            String mvnOpts = ""
            if (config.mvnProfiles) {
                mvnOpts <<= "-P ${config.mvnProfiles}"
            }
            if (config.mavenOptions) {
                mvnOpts <<= " ${config.mavenOptions}"
            }

            echo "\n============== Exécution du plugin sonar:sonar ========================="
            echo "\n== Sonar Options : ${sonarOpts}"
            echo "\n== Maven Options : ${mvnOpts}"

            sh "mvn -gs ${env['MAVEN_PE_SETTINGS']} -f ${config.pom} ${sonarOpts} ${mvnOpts} sonar:sonar"

            def sonarProjectProps = readProperties file: "${pwd()}/target/sonar/report-task.txt"
            println "sonarProjectProps = " + sonarProjectProps.toString()
            // convertis en String Json, le but étant de passer plusieurs variables dans un seule variable globale Jenkins
            /* env['MYSONAR_PROJECT_PROPS'] = "{ "
            sonarProjectProps.each{ k, v ->
                env['MYSONAR_PROJECT_PROPS'] += "\"${k}\":\"${v}\", "
            }
            env['MYSONAR_PROJECT_PROPS'] = env['MYSONAR_PROJECT_PROPS'].substring(0, env['MYSONAR_PROJECT_PROPS'].length()-2) + " }"*/
            env.MYSONAR_PROJECT_PROPS = buildProjectProps(sonarProjectProps)
        }
    }

@NonCPS
def buildProjectProps(def sonarProjectProps) {
    def jsonProps = "{ "
    sonarProjectProps.each{ k, v ->
        jsonProps += "\"${k}\":\"${v}\", "
    }
    jsonProps = jsonProps.substring(0, jsonProps.length()-2) + " }"

    return jsonProps
}