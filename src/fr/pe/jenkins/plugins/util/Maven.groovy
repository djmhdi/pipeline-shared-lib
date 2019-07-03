#!/usr/bin/groovy
package fr.pe.jenkins.plugins.util

enum Maven {
	MAVEN2('MAVEN_2_2_1_HOME'), MAVEN3('MAVEN_3_3_9_HOME')

	Maven(String labelMvn) {
		this.label = labelMvn
	}
	private final String label

	public String toString() {
		return this.label
	}
}