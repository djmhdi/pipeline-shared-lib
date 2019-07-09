#!/usr/bin/groovy
package com.capgemini.fs.jenkins.plugins.deploy.command

import java.util.logging.Logger
import com.capgemini.fs.jenkins.plugins.ssh.*
import com.capgemini.fs.jenkins.plugins.util.FadiaScripts

import static com.capgemini.fs.jenkins.plugins.ssh.OpenSSHCommand.*
import static com.capgemini.fs.jenkins.plugins.ssh.SudoUCommand.*
import static com.capgemini.fs.jenkins.plugins.ssh.InstallFadiaCommand.*
import static com.capgemini.fs.jenkins.plugins.ssh.FadiaTwsCommand.*

class InstallConso implements Serializable {

	static Logger logger = Logger.getLogger('com.capgemini.fs.jenkins.plugins.deploy.command.InstallConso')

	def execute(script, def bundle, def pvnTarget, def pvnDictionary) {
		String codeEnveloppe = pvnDictionary["fadia.codeEnveloppe"]
		String codeCadre = pvnDictionary["fadia.cadre"]
		String codeAppli = pvnDictionary["fadia.appli"]
		String codeDomaine = pvnDictionary["fadia.domaine"]

		String installUser = "${codeCadre}${codeAppli}"
		if (codeEnveloppe == 'sd' || codeEnveloppe == 'dk') installUser = "x${codeAppli}"

		String fadiaCommand = FadiaScripts.INSTALL_CONSO.cmd
		String[] fadiaOpts = ["${codeAppli}", "*${bundle.artifactId}"] as String[]

		InstallFadiaCommand fadia = installFadia(fadiaCommand, fadiaOpts)
		FadiaTwsCommand tws = twsLike(codeCadre, codeDomaine, null, fadia)

		String cmd = openSSH(pvnTarget.host, false, "xjenki").chain(sudo(installUser).chain(tws)).returnCommand()

		script.sh cmd
	}
}