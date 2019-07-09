#!/usr/bin/groovy
package com.capgemini.fs.jenkins.plugins.quality

enum SonarInstances {
	SONAR_DMC_SPN('sonar-dmc-spn'),
	SONAR_DROSD_ODT('sonar-drosd-odt')

    SonarInstances(String instanceName) {
		this.name = instanceName
	}
	private final String name

	public String toString() {
		return this.name
	}
}