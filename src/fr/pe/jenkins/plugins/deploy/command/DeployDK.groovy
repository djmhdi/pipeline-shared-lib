#!/usr/bin/groovy
package fr.pe.jenkins.plugins.deploy.command

class DeployDK extends DeployDispatcher {

    @Override
    String getInstallUser() {
        return "x${codeAppli}"
    }

    @Override
    String getAppliCibleArg() {
        return "-a ${codeAppliDocker}"
    }
}