#!/usr/bin/groovy
package com.capgemini.fs.jenkins.plugins.restart.command

class RestartTomcat extends RestartProdopCommand {

//    static Logger logger = Logger.getLogger('com.capgemini.fs.jenkins.plugins.restart.command.RestartTomcat')

//    def execute(script, def targetDescriptor) {
//        script.sh """ ssh -l xjenki ${targetDescriptor.host} "sudo su - ${targetDescriptor.owner} -c 'stop.Tomcat.sh'" """
//        script.sh """ ssh -l xjenki ${targetDescriptor.host} "sudo su - ${targetDescriptor.owner} -c 'start.Tomcat.sh'" """
//    }

    @Override
    String getPrefix() {
        return "ki"
    }

    @Override
    String getRestartType() {
        return "TOMCAT"
    }
}

