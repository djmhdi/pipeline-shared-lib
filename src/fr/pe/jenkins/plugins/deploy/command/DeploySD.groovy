#!/usr/bin/groovy
package fr.pe.jenkins.plugins.deploy.command

class DeploySD extends DeployDispatcher {

    @Override
    String getInstallUser() {
        return "x${codeAppli}"
    }

    @Override
    String getAppliCibleArg() {
        return "-a ${codeAppliDocker}"
    }

    @Override
    def execute(script, def bundle, def pvnTarget, def pvnDictionary, def deployOptions) {
        execute(script, bundle, pvnTarget, pvnDictionary, deployOptions, true)
    }
}