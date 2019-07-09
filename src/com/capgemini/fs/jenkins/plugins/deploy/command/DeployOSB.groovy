#!/usr/bin/groovy
package com.capgemini.fs.jenkins.plugins.deploy.command

import java.util.logging.Logger
import com.capgemini.fs.jenkins.plugins.ssh.*
import com.capgemini.fs.jenkins.plugins.util.FadiaScripts

import static com.capgemini.fs.jenkins.plugins.ssh.OpenSSHCommand.*
import static com.capgemini.fs.jenkins.plugins.ssh.SudoUCommand.*
import static com.capgemini.fs.jenkins.plugins.ssh.InstallFadiaCommand.*
import static com.capgemini.fs.jenkins.plugins.ssh.FadiaTwsCommand.*

class DeployOSB implements Serializable {

	static Logger logger = Logger.getLogger('com.capgemini.fs.jenkins.plugins.deploy.command.DeployOSB')

	def execute(script, def bundle, def pvnTarget, def pvnDictionary, def deployOptions) {
		String codeCadre = pvnDictionary["fadia.cadre"]
		String codeAppli = pvnDictionary["fadia.appli"]
		String codeDomaine = pvnDictionary["fadia.domaine"]
		String codeEnveloppe = pvnDictionary["fadia.codeEnveloppe"]
		String osbUrl = pvnDictionary["osb.admin.url"]
		String osbUser = pvnDictionary["osb.admin.user"]
		String osbPwd = pvnDictionary["osb.admin.pwd"]

		String fadiaCommand = FadiaScripts.INSTALL_SPEC_OSB.cmd
		String[] fadiaOpts = ["${codeAppli}", "${codeAppli}", "dme${codeEnveloppe}", "-Djmx.serial.form=1.0"] as String[]
		String[] envOpts = ["INSTWA4=${pvnTarget.numInstance}", "OSBURL=${osbUrl}", "OSBUSER=${osbUser}", "OSBPWD=${osbPwd}"]

		InstallFadiaCommand fadia = installFadia(fadiaCommand, fadiaOpts)
		FadiaTwsCommand tws = twsLike(codeCadre, codeDomaine, envOpts, fadia)

		String cmd = openSSH(pvnTarget.host, true, "xjenki").chain(sudo(pvnTarget.owner).chain(tws)).returnCommand()

		script.sh cmd
	}
}