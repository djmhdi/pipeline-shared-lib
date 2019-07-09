#!/usr/bin/groovy
package com.capgemini.fs.jenkins.plugins.ssh

import java.util.logging.Logger
import com.capgemini.fs.jenkins.plugins.ssh.Command;
import com.cloudbees.groovy.cps.NonCPS

class FadiaTwsCommand extends AbstractCommand {

	static Logger logger = Logger.getLogger('com.capgemini.fs.jenkins.plugins.ssh.FadiaTwsCommand')

	private FadiaTwsCommand(String cadre, String domaine, String[] envOpts, InstallFadiaCommand fadiaCommand) {
		command = "/applis/xfadia/pur/trt/fadia.z.install_lanceTWSlike.ksh -e ENVT=${cadre} "
		if (domaine) command <<= "-e DOMAIN=${domaine} "

		for (String opt : envOpts) {
			command <<= "-e ${opt} "
		}

		command <<= "-f '"
		command <<= fadiaCommand.returnCommand()
		command <<= "' "

	}

    @NonCPS
	static twsLike(String cadre, String domaine, String[] envOpts, InstallFadiaCommand fadiaCommand) {
		return new FadiaTwsCommand(cadre, domaine, envOpts, fadiaCommand)
	}
}