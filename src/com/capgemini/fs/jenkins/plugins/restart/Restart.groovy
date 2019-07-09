#!/usr/bin/groovy
package com.capgemini.fs.jenkins.plugins.restart

import java.util.logging.Logger

class Restart {

	static Logger logger = Logger.getLogger('com.capgemini.fs.jenkins.plugins.restart.Restart')

	static def serverValidForRestart = ['tomcat', 'apache', 'weblogic', 'nuxeo']

	static def restart(script, def targetDescriptor, def dictionary) {
		def type = targetDescriptor.type

        // on vérifie si le type extrait de la target de la matrice est surchargée dans le dico
        // exemple pour nuxeo installé sur une base de tomcat PE
        String typeDico = dictionary["ic.restart.type"]
        if (typeDico){
            type = typeDico.toLowerCase()
        }

		if (serverValidForRestart.contains(type)){
			def restartCommand = "Restart" + type.substring(0,1).toUpperCase() + type.substring(1).toLowerCase()
			logger.info("Redémarrage ${targetDescriptor}")
			script.sh "echo Redémarrage $targetDescriptor"

			def starter = script.getClass().classLoader.loadClass( "com.capgemini.fs.jenkins.plugins.restart.command.$restartCommand", true, false )?.newInstance()

            // Si la liste des instances à redémarrer est configurée dans le dico de la ligne de la matrice on l'utilise,
            // sinon on tire les infos de la target de la ligne de la matrice.
            // Utile dans le cas des weblo où on livre sur l'admin et on redémarre les managés
            String instancesToRestart = dictionary["ic.restart.instances"]
            if (instancesToRestart){
                script.sh """ echo "Liste instances à redémarrer : $instancesToRestart" """

                String[] instances = instancesToRestart.split(';')

                if (instances){

                    for (int i = 0; i < instances.length; i++) {
                        String instance = instances[i]
                        script.sh """ echo "Restart $instance" """

                        String[] instancePart = instance.split(':')
                        targetDescriptor.host = instancePart[0]
                        targetDescriptor.instance = instancePart[1]

                        starter.execute(script, targetDescriptor)
                    }
                }
            }else{
                starter.execute(script, targetDescriptor)
            }

		}else{
			script.sh "echo Impossible de redémarrer les targets de type $type"
		}
	}
//	static def restart(script, def host, def environnement, def descriptor) {
//		def type = "${environnement.restart.type}"
//		def restartCommand = "Restart" + type.substring(0,1).toUpperCase() + type.drop(1).toLowerCase()
//		logger.info("Redémarrage ${host}")
//		script.sh "echo $host - $restartCommand"
//
//		def starter = script.getClass().classLoader.loadClass( "com.capgemini.fs.jenkins.plugins.restart.command.$restartCommand", true, false )?.newInstance()
//
//		starter.execute script, host, environnement, descriptor
//	}
}