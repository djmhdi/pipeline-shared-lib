#!/usr/bin/groovy
package com.capgemini.fs.jenkins.plugins.deploy.command

import com.capgemini.fs.jenkins.plugins.ssh.FadiaTwsCommand
import com.capgemini.fs.jenkins.plugins.ssh.InstallFadiaCommand
import com.capgemini.fs.jenkins.plugins.util.FadiaScripts

import static com.capgemini.fs.jenkins.plugins.ssh.FadiaTwsCommand.twsLike
import static com.capgemini.fs.jenkins.plugins.ssh.InstallFadiaCommand.installFadia
import static com.capgemini.fs.jenkins.plugins.ssh.OpenSSHCommand.openSSH
import static com.capgemini.fs.jenkins.plugins.ssh.SudoUCommand.sudo

abstract class DeployWx implements Serializable {

	def execute(script, def bundle, def pvnTarget, def pvnDictionary, def deployOptions) {
		String codeCadre = pvnDictionary["fadia.cadre"]
		String codeAppli = pvnDictionary["fadia.appli"]
		String codeDomaine = pvnDictionary["fadia.domaine"]
		String codeEnveloppe = pvnDictionary["fadia.codeEnveloppe"]
		String cheminBundle = pvnDictionary["fadia.cheminBundle"]

		String type = cheminBundle.substring(0,2)

		String fadiaCommand = FadiaScripts.INSTALL_COPIE_INSTANCE.cmd
		String[] fadiaOpts = ["${codeAppli}", "${codeAppli}", "${type}${codeCadre}${codeDomaine}${pvnTarget.numInstance}", "dme${codeEnveloppe}"] as String[]

		InstallFadiaCommand fadia = installFadia(fadiaCommand, fadiaOpts)
		FadiaTwsCommand tws = twsLike(codeCadre, codeDomaine, null, fadia)

		String cmd = openSSH(pvnTarget.host, true, "xjenki").chain(sudo(pvnTarget.owner).chain(tws)).returnCommand()

		script.sh cmd
	}

}