#!/usr/bin/groovy
package com.capgemini.fs.jenkins.plugins.restart.command

class RestartNuxeo implements Serializable {

    def execute(script, def targetDescriptor) {

        // les commandes executables en sudo par xjenki sont limitées :
        // si le user commence par kat il faut que la commande contienne *Tomcat*
        // si le user commence par wls il faut que la commande contienne *Weblogic*
        // ... voir dans le fichier /etc/sudoers sur une VM
        script.sh """ ssh -l xjenki ${targetDescriptor.host} "sudo su - ${targetDescriptor.owner} -c 'nuxeoctl restartbg; echo \'Tomcat\''" """
    }
}

