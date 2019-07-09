#!/usr/bin/groovy
package com.capgemini.fs.jenkins.plugins.notification

import com.cloudbees.groovy.cps.NonCPS

class NotificationTemplate implements Serializable {

	private String subjectModel
	private String contentModel

	private NotificationTemplate(String thisSubjectModel, String thisContentModel) {
		this.subjectModel = thisSubjectModel
		this.contentModel = thisContentModel
	}

	@NonCPS
	static NotificationTemplate with(String thisSubjectModel, String thisContentModel) {
		return new NotificationTemplate(thisSubjectModel, thisContentModel)
	}
}
