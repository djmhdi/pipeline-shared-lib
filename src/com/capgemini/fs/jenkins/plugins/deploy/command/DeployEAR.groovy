#!/usr/bin/groovy
package com.capgemini.fs.jenkins.plugins.deploy.command

import com.capgemini.fs.jenkins.plugins.ssh.FadiaTwsCommand
import com.capgemini.fs.jenkins.plugins.ssh.InstallFadiaCommand
import com.capgemini.fs.jenkins.plugins.util.FadiaScripts

import java.util.logging.Logger

import static com.capgemini.fs.jenkins.plugins.ssh.FadiaTwsCommand.twsLike
import static com.capgemini.fs.jenkins.plugins.ssh.InstallFadiaCommand.installFadia
import static com.capgemini.fs.jenkins.plugins.ssh.OpenSSHCommand.openSSH
import static com.capgemini.fs.jenkins.plugins.ssh.SudoUCommand.sudo

abstract class DeployEAR implements Serializable {

	static Logger logger = Logger.getLogger('com.capgemini.fs.jenkins.plugins.deploy.command.DeployEAR')

	def execute(script, def bundle, def pvnTarget, def pvnDictionary, def deployOptions) {
		String codeCadre = pvnDictionary["fadia.cadre"]
		String codeAppli = pvnDictionary["fadia.appli"]
		String codeDomaine = pvnDictionary["fadia.domaine"]
		String codeEnveloppe = pvnDictionary["fadia.codeEnveloppe"]
		String nomArtefact = pvnDictionary["fadia.nomArtefact"]

		String fadiaCommand
		String[] fadiaOpts

		if (!nomArtefact || nomArtefact ==~ /.*_.*\d*\.\d*\.\d*\.ear/) {
			fadiaCommand = FadiaScripts.INSTALL_EAR.cmd
			fadiaOpts = ["${codeAppli}", "${pvnTarget.numInstance}", "dme${codeEnveloppe}", "${codeAppli}", "${deployOptions}"] as String[]
		} else {
			fadiaCommand = FadiaScripts.INSTALL_COPIE_EAR.cmd
			fadiaOpts = ["${codeAppli}", "${codeAppli}", "dme${codeEnveloppe}", "${pvnTarget.numInstance}", "${deployOptions}"] as String[]
		}

		InstallFadiaCommand fadia = installFadia(fadiaCommand, fadiaOpts)
		FadiaTwsCommand tws = twsLike(codeCadre, codeDomaine, null, fadia)

		String cmd = openSSH(pvnTarget.host, true, "xjenki").chain(sudo(pvnTarget.owner).chain(tws)).returnCommand()
		logger.info(cmd)

		if (script) {
			script.sh cmd
		}

		return cmd
	}
}