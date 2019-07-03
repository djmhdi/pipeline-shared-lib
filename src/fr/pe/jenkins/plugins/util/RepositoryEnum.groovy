#!/usr/bin/groovy
package fr.pe.jenkins.plugins.util

enum RepositoryEnum {
	ALPHA('Alpha', "REPO_ALPHA_URL", "repo-alpha"),
	BETA('Beta', "REPO_BETA_URL", "repo-beta"),
	RC('RC', "REPO_RC_URL", "repo-rc"),
	PROD('Prod', "REPO_PROD_URL", "repo-prod"),
	POLEEMPLOI('PoleEmploi', "REPO_POLE_EMPLOI_URL", "repo-poleemploi")
	
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