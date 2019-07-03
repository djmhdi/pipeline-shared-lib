#!/usr/bin/groovy
package fr.pe.jenkins.plugins.deploy.command

import java.util.logging.Logger
import fr.pe.jenkins.plugins.ssh.*
import fr.pe.jenkins.plugins.util.FadiaScripts

import static fr.pe.jenkins.plugins.ssh.OpenSSHCommand.*
import static fr.pe.jenkins.plugins.ssh.SudoUCommand.*
import static fr.pe.jenkins.plugins.ssh.InstallFadiaCommand.*
import static fr.pe.jenkins.plugins.ssh.FadiaTwsCommand.*

class DeployTY implements Serializable {

    static Logger logger = Logger.getLogger('fr.pe.jenkins.plugins.deploy.command.DeployTY')

    def execute(script, def bundle, def pvnTarget, def pvnDictionary, def deployOptions) {
        String codeCadre = pvnDictionary["fadia.cadre"]
        String codeAppli = pvnDictionary["fadia.appli"]
        String codeDomaine = pvnDictionary["fadia.domaine"]
        String codeEnveloppe = pvnDictionary["fadia.codeEnveloppe"]
        String nomArtefact = pvnDictionary["fadia.nomArtefact"]

        String fadiaCommand
        String[] fadiaOpts

        if (!nomArtefact || nomArtefact ==~ /.*_.*\d*\.\d*\.\d*\.ear/) {
            fadiaCommand = FadiaScripts.INSTALL_EAR.cmd
            fadiaOpts = ["${codeAppli}", "${pvnTarget.numInstance}", "dme${codeEnveloppe}", "${codeAppli}", "${deployOptions}"] as String[]
        } else {
            fadiaCommand = FadiaScripts.INSTALL_COPIE_EAR.cmd
            fadiaOpts = ["${codeAppli}", "${codeAppli}", "dme${codeEnveloppe}", "${pvnTarget.numInstance}", "${deployOptions}"] as String[]
        }

        InstallFadiaCommand fadia = installFadia(fadiaCommand, fadiaOpts)
        FadiaTwsCommand tws = twsLike(codeCadre, codeDomaine, null, fadia)


        String cmd = openSSH(pvnTarget.host, true, "xjenki").chain(sudo(pvnTarget.owner).chain(tws)).returnCommand()
        logger.info(cmd)

        if (script) {
            script.sh cmd
        }

        return cmd
    }

}