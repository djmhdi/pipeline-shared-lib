#!/usr/bin/groovy
package com.capgemini.fs.jenkins.plugins.util

enum Enveloppes {
	TAPESTRY('TY'), SLDNG('LG'), TOMCAT('WA'), PARAMETRAGE('SA'), OSB('OSB')

	Enveloppes(String typeEnveloppe) {
		this.type = typeEnveloppe
	}
	private final String type

	public String toString() {
		return this.type
	}
}