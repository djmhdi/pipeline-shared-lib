#!/usr/bin/groovy
package com.capgemini.fs.jenkins.plugins.util

enum Notifications {
	MAIL('mail'), SLACK('slack'), NONE('none')

	Notifications(String notificationType) {
		this.type = notificationType
	}
	private final String type

	public String toString() {
		return this.type
	}
}