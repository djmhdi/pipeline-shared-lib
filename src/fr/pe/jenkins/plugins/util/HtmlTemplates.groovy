#!/usr/bin/groovy
package fr.pe.jenkins.plugins.util

enum HtmlTemplates {
	HELLO('fr/pe/notification/html/HelloWorld.html'),
	CONSTRUCTION('fr/pe/notification/html/ResultatConstructionVersion.tmpl'),
	BUILD_SNAPSHOT('fr/pe/notification/html/ResultatBuildSnapshot.tmpl'),
	DEMANDE_LIVRAISON('fr/pe/notification/html/DemandeLivraison.tmpl'),
	LIVRAISON('fr/pe/notification/html/ResultatLivraison.tmpl'),
    PROMOTION('fr/pe/notification/html/ResultatPromotion.tmpl'),
    DEPLOIEMENT('fr/pe/notification/html/ResultatDeploiement.tmpl'),
	VERIFICATION_DEPLOIEMENT('fr/pe/notification/html/ResultatVerificationDeploiement.tmpl'),
	VERIFICATION_QUALITE('fr/pe/notification/html/ResultatVerificationQualite.tmpl')

    HtmlTemplates(String templatePath) {
		this.path = templatePath
	}
	private final String path

	public String toString() {
		return this.path
	}
}