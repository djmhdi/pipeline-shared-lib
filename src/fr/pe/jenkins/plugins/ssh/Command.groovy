#!/usr/bin/groovy
package fr.pe.jenkins.plugins.ssh

interface Command extends Serializable {

	public def chain(Command cmd)
	
	public def returnCommand()
}