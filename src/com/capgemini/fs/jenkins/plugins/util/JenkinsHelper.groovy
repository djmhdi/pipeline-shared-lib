#!/usr/bin/groovy
package com.capgemini.fs.jenkins.plugins.util

import com.cloudbees.groovy.cps.NonCPS
import hudson.model.Cause
import hudson.model.Cause.UpstreamCause

class JenkinsHelper {

	@NonCPS
	static def latestActorMessage(def currentBuild){
		//Get Latest Approuver
		def latestActorMessage
		def latestApprouver
		def acts = currentBuild.rawBuild.getAllActions()

		for (def act in acts) {
			if (act instanceof org.jenkinsci.plugins.workflow.support.steps.input.ApproverAction) {
				latestApprouver = act
			}
		}
		if (latestApprouver){
			latestActorMessage = "Approuved by user ${latestApprouver.userName}"
		} else if (currentBuild.rawBuild.getCause(Cause.UserIdCause)) {
			latestActorMessage = "${currentBuild.rawBuild.getCause(Cause.UserIdCause).shortDescription}"
		} else if (currentBuild.rawBuild.getCause(Cause.UpstreamCause)) {
			latestActorMessage = "${currentBuild.rawBuild.getCause(Cause.UpstreamCause).shortDescription}"
		} else {
			latestActorMessage = "not implemented for this case -_-"
		}
		latestApprouver = null

		return latestActorMessage
	}

	@NonCPS
	static def buildUserId(def currentBuild){
		//Get Latest Approuver
		def userId
		def latestApprover

		for (def act in currentBuild.rawBuild.getAllActions()) {
			if (act instanceof org.jenkinsci.plugins.workflow.support.steps.input.ApproverAction) {
				latestApprover = act
			}
		}
		if (latestApprover) {
			userId = latestApprover.userId
		}
		latestApprover = null

		return userId
	}
}