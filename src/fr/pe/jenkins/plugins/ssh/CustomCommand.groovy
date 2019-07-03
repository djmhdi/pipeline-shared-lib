#!/usr/bin/groovy
package fr.pe.jenkins.plugins.ssh

import com.cloudbees.groovy.cps.NonCPS

class CustomCommand extends AbstractCommand {

	private CustomCommand(String cmd) {
		command = cmd
    }

    @NonCPS
    def append(String elt) {
        command <<= elt
        return this
    }

	@NonCPS
	static createCommand(String cmd) {
		return new CustomCommand(cmd)
	}
}
