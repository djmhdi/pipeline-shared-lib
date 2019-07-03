#!/usr/bin/groovy
package fr.pe.jenkins.plugins.ssh

import java.util.logging.Logger
import fr.pe.jenkins.plugins.ssh.Command;
import com.cloudbees.groovy.cps.NonCPS

class OpenSSHCommand extends AbstractCommand {

	static Logger logger = Logger.getLogger('fr.pe.jenkins.plugins.ssh.OpenSSHCommand')
	
	private OpenSSHCommand(String host, boolean hostKeyChecking, String login) {
		command = "ssh "
				
		if (!hostKeyChecking) {
			command <<= "-o StrictHostKeyChecking=no "
		}
		
		command <<= "-l $login $host "
		command <<='"'
    }
	
	def returnCommand() {
		command <<= '"'
		return this.command
	}

	@NonCPS
	static openSSH(String host, boolean hostKeyChecking = true, String login = "xjenki") {
		return new OpenSSHCommand(host, hostKeyChecking, login)
	}
}