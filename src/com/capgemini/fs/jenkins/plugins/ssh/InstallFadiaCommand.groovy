#!/usr/bin/groovy
package com.capgemini.fs.jenkins.plugins.ssh

import java.util.logging.Logger
import com.capgemini.fs.jenkins.plugins.ssh.Command;
import com.cloudbees.groovy.cps.NonCPS

class InstallFadiaCommand extends AbstractCommand {

	static Logger logger = Logger.getLogger('com.capgemini.fs.jenkins.plugins.ssh.InstallFadiaCommand')

	private InstallFadiaCommand(String fadiaScript, String[] fadiaOpts) {
		command = "$fadiaScript "
				
		for (String opt : fadiaOpts) {
			command <<= "${opt} "
		}
	}
				
	@NonCPS
	static installFadia(String fadiaScript, String[] fadiaOpts) {
		return new InstallFadiaCommand(fadiaScript, fadiaOpts)
	}
}