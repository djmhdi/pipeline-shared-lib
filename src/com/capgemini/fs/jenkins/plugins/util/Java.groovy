#!/usr/bin/groovy
package com.capgemini.fs.jenkins.plugins.util

enum Java {
	JDK6('JAVA_1_6_0_45_HOME'), JDK7('JAVA_1_7_0_67_HOME'), JDK8('JAVA_1_8_0_66_HOME')

	Java(String labelJdk) {
		this.label = labelJdk
	}
	private final String label

	public String toString() {
		return this.label
	}
}