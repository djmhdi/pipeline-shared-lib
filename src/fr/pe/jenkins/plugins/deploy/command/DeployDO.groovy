#!/usr/bin/groovy
package fr.pe.jenkins.plugins.deploy.command

import fr.pe.jenkins.plugins.ssh.FadiaTwsCommand
import fr.pe.jenkins.plugins.ssh.InstallFadiaCommand
import fr.pe.jenkins.plugins.util.FadiaScripts

import java.util.logging.Logger

import static fr.pe.jenkins.plugins.ssh.FadiaTwsCommand.twsLike
import static fr.pe.jenkins.plugins.ssh.InstallFadiaCommand.installFadia
import static fr.pe.jenkins.plugins.ssh.OpenSSHCommand.openSSH
import static fr.pe.jenkins.plugins.ssh.SudoUCommand.sudo

class DeployDO implements Serializable {

	static Logger logger = Logger.getLogger('fr.pe.jenkins.plugins.deploy.command.DeployDO')

	def execute(script, def bundle, def pvnTarget, def pvnDictionary, def deployOptions) {
		String codeCadre = pvnDictionary["fadia.cadre"]
		String codeAppli = pvnDictionary["fadia.appli"]

		String fadiaCommand = FadiaScripts.INSTALL_COPIE_DONAPP.cmd
		InstallFadiaCommand fadiaBin = installFadia(fadiaCommand, ["${codeAppli}", "${codeAppli}", "bin", "${deployOptions}"] as String[])
		InstallFadiaCommand fadiaPar = installFadia(fadiaCommand, ["${codeAppli}", "${codeAppli}", "par", "${deployOptions}"] as String[])
		InstallFadiaCommand fadiaSql = installFadia(fadiaCommand, ["${codeAppli}", "${codeAppli}", "sql", "${deployOptions}"] as String[])
		InstallFadiaCommand fadiaTrt = installFadia(fadiaCommand, ["${codeAppli}", "${codeAppli}", "trt", "${deployOptions}"] as String[])

		String[] cbn = ["CBN=00"] as String[]
		FadiaTwsCommand twsBin = twsLike(codeCadre, null, cbn, fadiaBin)
		FadiaTwsCommand twsPar = twsLike(codeCadre, null, cbn, fadiaPar)
		FadiaTwsCommand twsSql = twsLike(codeCadre, null, cbn, fadiaSql)
		FadiaTwsCommand twsTrt = twsLike(codeCadre, null, cbn, fadiaTrt)

		String sudoer = "${codeCadre}${codeAppli}00"
		String cmdBin = openSSH(pvnTarget.host, true, "xjenki").chain(sudo(sudoer).chain(twsBin)).returnCommand()
		String cmdPar = openSSH(pvnTarget.host, true, "xjenki").chain(sudo(sudoer).chain(twsPar)).returnCommand()
		String cmdSql = openSSH(pvnTarget.host, true, "xjenki").chain(sudo(sudoer).chain(twsSql)).returnCommand()
		String cmdTrt = openSSH(pvnTarget.host, true, "xjenki").chain(sudo(sudoer).chain(twsTrt)).returnCommand()

		script.sh cmdBin
		script.sh cmdPar
		script.sh cmdSql
		script.sh cmdTrt
	}
}