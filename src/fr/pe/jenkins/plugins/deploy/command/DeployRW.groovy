#!/usr/bin/groovy
package fr.pe.jenkins.plugins.deploy.command

import fr.pe.jenkins.plugins.util.FadiaScripts

import java.util.logging.Logger

import static fr.pe.jenkins.plugins.ssh.OpenSSHCommand.openSSH
import static fr.pe.jenkins.plugins.ssh.SudoUCommand.sudo

class DeployRW implements Serializable {

	static Logger logger = Logger.getLogger('fr.pe.jenkins.plugins.deploy.command.DeployRW')

	def execute(script, def bundle, def pvnTarget, def pvnDictionary, def deployOptions) {
		String codeCadre = pvnDictionary["fadia.cadre"]
		String codeAppli = pvnDictionary["fadia.appli"]
		String codeDomaine = pvnDictionary["rp.code.domaine"]
		String codeEnveloppe = pvnDictionary["fadia.codeEnveloppe"]

		String fadiaCommand = "/applis/xrpweb/pur/trt/install-rp-ng/" +  FadiaScripts.INSTALL_RWEB.cmd + " ${codeAppli}"
		
		String cmd = openSSH(pvnTarget.host, true, "xjenki").chain(sudo("${codeCadre}${codeAppli}").chain(fadiaCommand)).returnCommand()
		
		script.sh cmd
	}
}
