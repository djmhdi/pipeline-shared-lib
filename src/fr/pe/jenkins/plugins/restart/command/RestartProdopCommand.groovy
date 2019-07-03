#!/usr/bin/groovy
package fr.pe.jenkins.plugins.restart.command

abstract class RestartProdopCommand implements Serializable {

    abstract String getPrefix()
    abstract String getRestartType()

    def execute(script, def targetDescriptor) {

        String typeUp = getRestartType()
        String instance
        if (targetDescriptor.instance){
            instance = targetDescriptor.instance
        }else{
            instance = getPrefix() + targetDescriptor.owner.drop(3)
        }

        script.sh """ ssh -o StrictHostKeyChecking=no -l xjenki ${targetDescriptor.host} "sudo /applis/xtechn/pur/trt/ut.z.ADM.ksh -o stop -a ${typeUp} -i ${instance}" """
        script.sh """ ssh -l xjenki ${targetDescriptor.host} "sudo /applis/xtechn/pur/trt/ut.z.ADM.ksh -o start -a ${typeUp} -i ${instance}" """
    }

}
