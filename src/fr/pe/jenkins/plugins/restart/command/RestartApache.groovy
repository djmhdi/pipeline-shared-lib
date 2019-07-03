#!/usr/bin/groovy
package fr.pe.jenkins.plugins.restart.command

class RestartApache extends RestartProdopCommand {

    @Override
    String getPrefix() {
        return "ai"
    }

    @Override
    String getRestartType() {
        return "APACHE"
    }
}

