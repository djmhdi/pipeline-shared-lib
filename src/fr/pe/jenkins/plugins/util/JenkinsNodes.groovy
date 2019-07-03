#!/usr/bin/groovy
package fr.pe.jenkins.plugins.util

enum JenkinsNodes {
	DEFAULT('maven'), MAVEN('maven'), RTC('rtc'), AIX7_GPE('AIX7-GPE'), NODEJS('nodejs')
	
	JenkinsNodes(String nodeLabel) {
		this.label = nodeLabel
	}
	private final String label

	public String toString() {
		return this.label
	}
}