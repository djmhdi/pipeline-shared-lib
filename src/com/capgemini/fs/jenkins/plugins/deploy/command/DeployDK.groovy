#!/usr/bin/groovy
package com.capgemini.fs.jenkins.plugins.deploy.command

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