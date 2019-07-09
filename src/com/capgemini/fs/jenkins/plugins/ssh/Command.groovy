#!/usr/bin/groovy
package com.capgemini.fs.jenkins.plugins.ssh

interface Command extends Serializable {

	public def chain(Command cmd)
	
	public def returnCommand()
}