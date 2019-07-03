#!/usr/bin/groovy
package fr.pe.jenkins.plugins.deploy

import java.util.logging.Logger
import fr.pe.jenkins.plugins.deploy.command.*

class PrepaDeployConso {

	static Logger logger = Logger.getLogger('fr.pe.jenkins.plugins.deploy.PrepaDeployConso')

	static def deploy(script, def bundle, def pvnTarget, def pvnDictionary, def deployOptions) {
        String deployType = pvnDictionary["ic.deploy.type"]

        if (!deployType) {
            deployType = pvnDictionary["fadia.codeEnveloppe"]
        }

		String deployClass = "Deploy" + deployType.toUpperCase()

		def installPrepa = new InstallPrepa()
		def deploy = script.getClass().classLoader.loadClass( "fr.pe.jenkins.plugins.deploy.command.${deployClass}", true, false )?.newInstance()
		def installConso = new InstallConso()

		installPrepa.execute(script, bundle, pvnTarget, pvnDictionary)
		deploy.execute(script, bundle, pvnTarget, pvnDictionary, deployOptions)
		installConso.execute(script, bundle, pvnTarget, pvnDictionary)
	}
}