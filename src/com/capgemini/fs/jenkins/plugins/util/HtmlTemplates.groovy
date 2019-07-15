#!/usr/bin/groovy
package com.capgemini.fs.jenkins.plugins.util

enum HtmlTemplates {
	HELLO('com/capgemini/fs/notification/html/HelloWorld.html'),
	CONSTRUCTION('com/capgemini/fs/notification/html/ResultatConstructionVersion.tmpl'),
	BUILD_SNAPSHOT('com/capgemini/fs/notification/html/ResultatBuildSnapshot.tmpl'),
	DEMANDE_LIVRAISON('com/capgemini/fs/notification/html/DemandeLivraison.tmpl'),
	LIVRAISON('com/capgemini/fs/notification/html/ResultatLivraison.tmpl'),
    PROMOTION('com/capgemini/fs/notification/html/ResultatPromotion.tmpl'),
    DEPLOIEMENT('com/capgemini/fs/notification/html/ResultatDeploiement.tmpl'),
	VERIFICATION_DEPLOIEMENT('com/capgemini/fs/notification/html/ResultatVerificationDeploiement.tmpl'),
	VERIFICATION_QUALITE('com/capgemini/fs/notification/html/ResultatVerificationQualite.tmpl')

    HtmlTemplates(String templatePath) {
		this.path = templatePath
	}
	private final String path

	public String toString() {
		return this.path
	}
}