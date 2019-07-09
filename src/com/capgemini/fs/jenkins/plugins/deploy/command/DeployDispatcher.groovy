#!/usr/bin/groovy
package com.capgemini.fs.jenkins.plugins.deploy.command

import com.capgemini.fs.jenkins.plugins.ssh.*
import com.capgemini.fs.jenkins.plugins.util.FadiaScripts

import static com.capgemini.fs.jenkins.plugins.ssh.OpenSSHCommand.*
import static com.capgemini.fs.jenkins.plugins.ssh.InstallFadiaCommand.*
import static com.capgemini.fs.jenkins.plugins.ssh.FadiaTwsCommand.*

abstract class DeployDispatcher implements Serializable {

	String codeCadre, codeAppli, codeAppliDocker

	String getInstallUser() {
		return "${codeCadre}${codeAppli}"
	}

	String getAppliCibleArg() {
		return null
	}

	def execute(script, def bundle, def pvnTarget, def pvnDictionary, def deployOptions, boolean sudosu) {
		String codeEnveloppe = pvnDictionary["fadia.codeEnveloppe"]
		this.codeCadre = pvnDictionary["fadia.cadre"]
		this.codeAppli = pvnDictionary["fadia.appli"]
		this.codeAppliDocker = pvnDictionary["fadia.docker.appli"]

		String fadiaCommand = FadiaScripts.INSTALL_SPEC_DISPATCHER.cmd
		List<String> fadiaOpts = ["-t dme${codeEnveloppe}", "-s ${codeAppli}"]
		if (this.getAppliCibleArg()) fadiaOpts.add(this.getAppliCibleArg())
		fadiaOpts.add("${deployOptions}")

		InstallFadiaCommand fadia = installFadia(fadiaCommand, fadiaOpts as String[])
		FadiaTwsCommand tws = twsLike(codeCadre, null, null, fadia)

		Command sudoCommand
		if (sudosu) {
			sudoCommand = ((SudoSUCommand) SudoSUCommand.sudo(this.getInstallUser()).chain(tws)).endCommand()
		} else {
			sudoCommand = SudoUCommand.sudo(this.getInstallUser()).chain(tws)
		}

		String cmd = openSSH(pvnTarget.host, true, "xjenki").chain(sudoCommand).returnCommand()

		script.sh cmd
	}

	def execute(script, def bundle, def pvnTarget, def pvnDictionary, def deployOptions) {
		execute(script, bundle, pvnTarget, pvnDictionary, deployOptions, false)
	}
}