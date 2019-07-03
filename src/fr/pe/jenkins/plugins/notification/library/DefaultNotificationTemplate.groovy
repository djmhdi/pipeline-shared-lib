#!/usr/bin/groovy
package fr.pe.jenkins.plugins.notification.library

enum DefaultNotificationTemplate implements Serializable {
	HELLO('HelloWorld'),
	CONSTRUCTION('ResultatConstructionVersion'),
	BUILD_SNAPSHOT('ResultatBuildSnapshot'),
	DEMANDE_LIVRAISON('DemandeLivraison'),
	LIVRAISON('ResultatLivraison'),
	PROMOTION('ResultatPromotion'),
	DEPLOIEMENT('ResultatDeploiement'),
	VERIFICATION_DEPLOIEMENT('ResultatVerificationDeploiement'),
	VERIFICATION_QUALITE('ResultatVerificationQualite'),
	INFO_DEPLOIEMENT('InformationDeploiement')

	DefaultNotificationTemplate(String templateName) {
		this.name = templateName
	}

	private final String name

	public String toString() {
		return this.name
	}
}
