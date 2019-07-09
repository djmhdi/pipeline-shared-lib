#!/usr/bin/groovy
package com.capgemini.fs.jenkins.plugins.util

enum FadiaScripts {
	INSTALL_PREPA('fadia.z.install_prepa_n.ksh'),
	INSTALL_CONSO('fadia.z.install_conso_n.ksh'),
	INSTALL_EAR('fadia.z.install_ear.ksh'),
	INSTALL_COPIE_EAR('fadia.z.install_copie_ear.ksh'), 
	INSTALL_SPEC_OSB('fadia.z.install_spec_osb.ksh'), 
	INSTALL_COPIE_INSTANCE('fadia.z.install_copie_instance.ksh'),
	INSTALL_SPEC_DISPATCHER('fadia.z.install_spec-dispatcher.ksh'),
	INSTALL_COPIE_INSTANCE_APACHE('fadia.z.install_copie_instance_apache.ksh'),
	INSTALL_COPIE_DONAPP('fadia.z.install_copie_donapp.ksh'),
	INSTALL_COPIE('fadia.z.install_copie.ksh'),
	INSTALL_RWEB('fadia.z.install_spec-dmerw.internal.ksh')
	
	
	FadiaScripts(String fadiaCmd) {
		this.cmd = fadiaCmd
	}
	private final String cmd

	public String toString() {
		return this.cmd
	}
}