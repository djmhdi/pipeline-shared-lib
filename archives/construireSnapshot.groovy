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

    //--body
    println "-- config : " + config


    if (env.NODE_NAME) {
        echo "\nUtilisation du noeud  d'execution courant : [" + env.NODE_NAME + "]."
        createSnapshot(config)
    } else {
        echo "\nAllocation d'un nouveau noeud d'execution."
        if (!config.node) {
            config.node = JenkinsNodes.DEFAULT.label
        }
        node(config.node) {
            checkout scm
            createSnapshot(config)
        }
    }
}

def createSnapshot(config) {
    withEnv([
            "JAVA_HOME=${tool env.JAVA_VERSION_HOME}",
            "PATH+JAVA=${tool env.JAVA_VERSION_HOME}/bin",
            "PATH+MAVEN=${tool env.MAVEN_VERSION_HOME}/bin"
    ]) {
        try {
            echo "\n============== Build Snapshot ========================="

            // Construction de la commande mvn release perform
            String mvnCmd = "mvn -gs ${env['MAVEN_PE_SETTINGS']} -f ${config.parentPom} --batch-mode clean deploy "

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
            mvnCmd <<= "-DuseEditMode=true -Dresume=false "
            mvnCmd <<= " '"

/*
                mvnCmd <<= "-DaltDeploymentRepository=${config.repository.credentialId}::default::${env[config.repository.envParameterName]}' "
                mvnCmd <<= "-DreleaseVersion=${config.bundle.releaseVersion} -DdevelopmentVersion=${config.bundle.nextDevelopmentVersion}"
*/
            // Exécution de la commande mvn release perform
            sh "${mvnCmd}"

            // Création du bundle dans Proven
            createBundle(config.bundle, config.unzipArtefact, config.repository)

            currentBuild.result = 'SUCCESS'
        } catch (exception) {
            currentBuild.result = 'FAILURE'
            throw exception
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
        config.repository = RepositoryEnum.POLEEMPLOI
    }
}

def createBundle(def bundle, def unzipArtefact, def repository) {
    String artifactURL = bundle.getReleaseArchiveURL(env[repository.envParameterName])
    echo "\n============== Create Proven Bundle ========================="
    echo "ArtifactURL : ${artifactURL}"

    proven.createOrUpdateBundle {
        groupId = "${bundle.groupId}"
        artifactId = "${bundle.artifactId}"
        version = "${bundle.releaseVersion}"
        url = "${artifactURL}"
        promotionLevel = "${repository.label}"
        unzip = unzipArtefact
    }
}
