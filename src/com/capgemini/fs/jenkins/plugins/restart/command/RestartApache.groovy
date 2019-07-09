#!/usr/bin/groovy
package com.capgemini.fs.jenkins.plugins.restart.command

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

