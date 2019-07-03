#!/usr/bin/groovy
package fr.pe.jenkins.plugins.ssh

import java.util.logging.Logger

import com.cloudbees.groovy.cps.NonCPS

class SudoUCommand extends AbstractCommand {

	static Logger logger = Logger.getLogger('fr.pe.jenkins.plugins.ssh.SudoUCommand')
	
	String sudoer

	private SudoUCommand(String sudoer) {
		command = "sudo -u ${sudoer} "
    }

    @NonCPS
	static sudo(String sudoer) {
		return new SudoUCommand(sudoer)
	}
}