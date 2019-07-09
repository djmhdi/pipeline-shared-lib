#!/usr/bin/groovy
package com.capgemini.fs.jenkins.plugins.ssh

import com.cloudbees.groovy.cps.NonCPS

import java.util.logging.Logger

class SudoSUCommand extends AbstractCommand {

	static Logger logger = Logger.getLogger('com.capgemini.fs.jenkins.plugins.ssh.SudoSUCommand')

	String sudoer

	private SudoSUCommand(String sudoer) {
		command = "sudo su - ${sudoer} -c "
		command <<='\\"'
    }

	@NonCPS
	def endCommand() {
		this.command <<= '\\"'
		return this
	}

    @NonCPS
	static sudo(String sudoer) {
		return new SudoSUCommand(sudoer)
	}
}