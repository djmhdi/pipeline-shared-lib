#!/usr/bin/groovy
import com.capgemini.fs.jenkins.plugins.notification.NotificationDataKeys

import static com.capgemini.fs.jenkins.plugins.deploy.CheckDeployment.*
import static com.capgemini.fs.jenkins.plugins.proven.ProvenResource.*
import static com.capgemini.fs.jenkins.plugins.deploy.PrepaDeployConso.*
import com.capgemini.fs.jenkins.plugins.util.*
import com.capgemini.fs.jenkins.plugins.bundle.Bundle
import static com.capgemini.fs.jenkins.plugins.restart.Restart.restart

/**
 * Permet de déployer un bundle sur un environnement
 * config.bundles : un tableau de bundles. ex: bundles = [releaseBundle]
 * config.environnement : l'environnement sur lequel déployé. ex : environnement.TIC en considérant qu'environnement
 * est un objet JSON, issu d'un fichier ou non, respectant la structure
 * {
 *    "TIC": {
 *        "matrix": "fr.pe.tech.test:jenkins-pipeline-test-fab:tic",
 *     }
 * }
 */
def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    check(config)

    node(config.node) {
        echo "Redémarrage lancé pour la matrice  :" + config.matrice

        withEnv([
                "PATH+JAVA=${tool env.JAVA_VERSION_HOME}/bin",
                "PATH+GROOVY=${tool env.GROOVY_VERSION_HOME}/bin",
                "PROVEN_instance=${env.PROVEN_instance}",
                "PROVEN_url=${env.PROVEN_url}",
        ]) {
            withCredentials([
                    [$class: 'UsernamePasswordMultiBinding', credentialsId: env.PROVEN_CREDENTIAL_ID, passwordVariable: 'provenPassword', usernameVariable: 'provenUser']
            ]) {
                try {
                    echo "\n============== Deploiement a chaud ========================="

                    def pvnMatrix = proven.getMatrix {
                        gav = config.matrice
                    }

                    echo "Matrix                : ${pvnMatrix.toString()} "
                    for (Bundle bundleDeployed : config.bundles) {
                        echo "Deploiement du Bundle             : ${bundleDeployed}"
                        deployerBundleAChaud(pvnMatrix, bundleDeployed, config.cluster)
                    }

                } catch (exception) {
                    println "-- dans exception" 
                    currentBuild.result = 'FAILURE'
                    throw exception
                } finally {
                    // On boucle sur les notifications et on y ajoute le resultat du deliver proven
/*                    for (int i = 0; i < config.notifications.size() ; i++) {
                        config.notifications[i].appendData(NotificationDataKeys.BUNDLES.key, config.bundles)
                        config.notifications[i].appendData(NotificationDataKeys.MATRICE.key, config.matrice)
                    }

                    notifier {
                        notifications = config.notifications
                    }
  */              }
            }
        }
    }
}    

def check(def config) {
    if (!config.node) {
        config.node = JenkinsNodes.DEFAULT.label
    }

    if (config.bundles?.size() < 1) {
        throw new Exception("le parametre 'bundles' est obligatoire !")
    }

    if (!config.matrice) {
        throw new Exception("le parametre 'matrice' est obligatoire !")
    }
}

def deployerBundleAChaud(def pvnMatrix, Bundle bundleDeployed, def cluster) {
    echo "Recherche du gav du dictionnaire pour le bundle : ${bundleDeployed.releaseGAV}"
    String dictionaryGAV = dictionaryGavFor bundleDeployed.releaseGAV, pvnMatrix

    echo "Recherche du dictionnaire pour le GAV : ${dictionaryGAV}"
    def dictionary = provenDictionary.get {
        gav = "${dictionaryGAV}"
    }

    def targets = targetsDescriptorFor bundleDeployed.releaseGAV, pvnMatrix
    def texte

    for (def target : targets) {
        println "--- install fadia :"
        deploy(this, bundleDeployed, target, dictionary, '')
        println "--- target : " + target.owner
        if(target.owner != '') {
            String scriptToExecute
    
            println "--- Ecrit le fichier deployer.sh dans le workspace du projet"
            if(cluster) {
                scriptToExecute = getScriptDeployerForCluster(target.owner, bundleDeployed.artifactId)
            } else {
                scriptToExecute = getScriptDeployer(target.owner, bundleDeployed.artifactId)
            }
            writeFile file: "deployer.sh", text: scriptToExecute
    
            println "--- scipts sh : " + scriptToExecute
            String machine = target.host
          
            sh """ssh -l xjenki ${machine} 'sh -s' < ./deployer.sh """
            println "--- scipt done --- " 
        }
    }
}


def getScriptDeployer(String instanceWeblo, String nomApplication){
  def stb = new StringBuilder()
  stb.append('#!/bin/sh\n')
  stb.append('# Script sh qui lance le deploiment a chaud d"un ear\n')
  stb.append('# Deux parametres :\n')
  stb.append('# => arg1 : instance weblo  ex : wlsba238\n')
  stb.append('# => arg2 : nom du livrable  ex : webguso-application\n')
  stb.append('sudo su - ').append(instanceWeblo).append(' -c \'\n')
  stb.append('nomLivrable=\'').append(nomApplication).append('\';\n')
  stb.append('idUser=$(whoami |sed -e "s/^wls//");\n')
  stb.append('fichierProperties=$PRJTOOLS/trv/wls.properties.${idUser}\n')
  stb.append('if [ ! -f ${fichierProperties} ]\n')
  stb.append('then\n')
  stb.append('echo "${fichierProperties} not found."\n')
  stb.append('fichierProperties=$PRJTOOLS/properties/wls.properties.${idUser}\n')
  stb.append('echo "${fichierProperties}"\n')
  stb.append('fi\n')
  stb.append('ficWlsProperties=$(ls -rt  ${fichierProperties} |tail -n 1);\n')
//  stb.append('ficWlsProperties=$(ls -rt  $PRJTOOLS/trv/wls.properties.${idUser}|tail -n 1);\n')
  stb.append('recupere(){\n')
  stb.append('     echo $(grep ^$1 ${ficWlsProperties} |sed "s/$1//");\n')
  stb.append('}\n')
  stb.append('repPar=$(recupere "PATH.DOMAIN.WLS=");\n')
  stb.append('ip=$(recupere "IP.WLS=");\n')
  stb.append('port=$(recupere "PORT.WLS=");\n')
  stb.append('utilisateur=$(recupere "USER.WLS=");\n')
  stb.append('motDePasse=$(recupere "PASSWORD.WLS=");\n')
  stb.append('target=$(recupere "SERVER.NAME.WLS=");\n')
  stb.append('echo $idUser\n')
  stb.append('echo $target\n')
  stb.append('echo $repPar\n')
  stb.append('echo $ip\n')
  stb.append('echo $port\n')
  stb.append('echo $utilisateur\n')
  stb.append('echo $motDePasse\n')
  stb.append('java weblogic.Deployer -adminurl t3://${ip}:${port} -user ${utilisateur} -password ${motDePasse} -deploy -name ${nomLivrable} -targets ${target} -source $(dirname ${repPar})/ear/${nomLivrable}.ear;\n')
  stb.append('#Weblogic\n')
  stb.append('\'\n')

  return stb.toString()
}

def getScriptDeployerForCluster(String instanceWeblo, String nomApplication){
  def stb = new StringBuilder()
  stb.append('#!/bin/sh\n')
  stb.append('# Script sh qui lance le deploiment a chaud d"un ear sur des clusters\n')
  stb.append('# Deux parametres :\n')
  stb.append('# => arg1 : instance weblo  ex : wlsba238\n')
  stb.append('# => arg2 : nom du livrable  ex : webguso-application\n')
  stb.append('sudo su - ').append(instanceWeblo).append(' -c \'\n')
  stb.append('nomLivrable=\'').append(nomApplication).append('\';\n')
  stb.append('idUser=$(whoami |sed -e "s/^wls//");\n')
  stb.append('ficWlsProperties=$(ls -rt  $PRJTOOLS/trv/wls.properties.${idUser}|tail -n 1);\n')
  stb.append('recupere(){\n')
  stb.append('     echo $(grep ^$1 ${ficWlsProperties} |sed "s/$1//");\n')
  stb.append('}\n')
  stb.append('serverName=$(recupere "SERVER.NAME.WLS=");\n')
  stb.append('if [[ $serverName == admi* ]]\n')
  stb.append('then\n')
  stb.append('repPar=$(recupere "PATH.DOMAIN.WLS=");\n')
  stb.append('ip=$(recupere "IP.WLS=");\n')
  stb.append('port=$(recupere "PORT.WLS=");\n')
  stb.append('utilisateur=$(recupere "USER.WLS=");\n')
  stb.append('motDePasse=$(recupere "PASSWORD.WLS=");\n')
  // Pour le cluster le target le trouve dans CLUSTER.NAME.LISTE au lieu de SERVER.NAME.WLS ; seul chgt significatif
  stb.append('target=$(recupere "CLUSTER.NAME.LISTE=");\n')
  stb.append('echo $idUser\n')
  stb.append('echo $target\n')
  stb.append('echo $repPar\n')
  stb.append('echo $ip\n')
  stb.append('echo $port\n')
  stb.append('echo $utilisateur\n')
  stb.append('echo $motDePasse\n')
  stb.append('java weblogic.Deployer -adminurl t3://${ip}:${port} -user ${utilisateur} -password ${motDePasse} -deploy -name ${nomLivrable} -targets ${target} -source $(dirname ${repPar})/ear/${nomLivrable}.ear;\n')
  stb.append('#Weblogic\n')
  stb.append('fi\n')
  stb.append('\'\n')
  return stb.toString()
}

