#!/usr/bin/groovy
package com.capgemini.fs.jenkins.plugins.ssh

import java.util.logging.Logger
import com.capgemini.fs.jenkins.plugins.ssh.Command;
import com.cloudbees.groovy.cps.NonCPS

abstract class AbstractCommand implements Command {

	static Logger logger = Logger.getLogger('com.capgemini.fs.jenkins.plugins.deploy.command.AbstractCommand')

    String command

	@NonCPS
	def chain(Command cmd) {
        command <<= cmd.returnCommand()
        return this
	}

    @NonCPS
	def returnCommand() {
		return this.command
	}
}