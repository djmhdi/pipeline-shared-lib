#!/usr/bin/groovy
package fr.pe.jenkins.plugins.util

enum PromotionStatus {
	QUALIFICATION_COMPOSANT('Alpha','Pret pour qualification composant'),
	QUALIFICATION_SYSTEME('Alpha','Pret pour qualification systeme'),
	VALIDATION_SECURITE('Alpha','Pret pour validation securite'),
	VALIDATION_ACCEPTANCE('Alpha','Pret pour validation acceptance'),
	VALIDATION_PERFORMANCE('Alpha','Pret pour validation performance'),
    ABANDON('Alpha', 'Release abandonnee'),
	PRODUCTION('Prod','Pret pour production'),
    ABANDON_PRODUCTION('Prod', 'Mise en production abandonnee')

	PromotionStatus(String thisRepositoryKey, String thisDescription) {
		this.repositoryKey = thisRepositoryKey
		this.description = thisDescription
	}

	public final String description
	public final String repositoryKey

	public String toString() {
		return name()
	}
}