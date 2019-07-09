#!/usr/bin/groovy
package com.capgemini.fs.jenkins.plugins.util

class EnvironnementUtils {

	static def getProjectName(def env){
		return env.JOB_NAME.substring(0, (env.JOB_NAME.indexOf('/') < 0) ? 0 : env.JOB_NAME.indexOf('/'))
	}
}