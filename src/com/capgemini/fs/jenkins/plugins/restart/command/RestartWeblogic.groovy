#!/usr/bin/groovy
package com.capgemini.fs.jenkins.plugins.restart.command

class RestartWeblogic extends RestartProdopCommand {

    @Override
    String getPrefix() {
        return "wi"
    }

    @Override
    String getRestartType() {
        return "WEBLOGIC"
    }
}
//
//class RestartWeblogic implements Serializable {
//
//    def execute(script, def host, def environnement, def descriptor) {
//
//        def managers = environnement.weblogic.manages
//
//        script.sh """ echo "Liste managés sur $host : $managers" """
//
//        if (managers){
//            managers.each {
//                script.sh """ echo "Restart ${it}" """
//                script.sh """ ssh -l xjenki ${host} "sudo /applis/xtechn/pur/trt/ut.z.ADM.ksh -o stop -a WEBLOGIC -i wi${descriptor.cadre}${descriptor.codeDomaine}${it}" """
//                script.sh """ ssh -l xjenki ${host} "sudo /applis/xtechn/pur/trt/ut.z.ADM.ksh -o start -a WEBLOGIC -i wi${descriptor.cadre}${descriptor.codeDomaine}${it}" """
//            }
//        }
//    }
//}

