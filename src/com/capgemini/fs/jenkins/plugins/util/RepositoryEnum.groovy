#!/usr/bin/groovy
package com.capgemini.fs.jenkins.plugins.util

enum RepositoryEnum {
	SNAPSHOT('Snapshot', "REPO_SNAPSHOT_URL", "maven-snapshots"),
	RELEASE('Release', "REPO_RELEASE_URL", "maven-releases"),
	PRODUCTION('Production', "REPO_PRODUCTION_URL", "maven-production")
	
	RepositoryEnum(String label, String envParameterName, String credentialId) {
		this.label = label
		this.envParameterName = envParameterName
		this.credentialId=credentialId
	}

	public final String label
	public final String envParameterName
	public final String credentialId
	
	public String toString() {
		return this.label
	}
}