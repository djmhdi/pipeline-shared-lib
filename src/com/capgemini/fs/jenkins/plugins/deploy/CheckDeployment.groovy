#!/usr/bin/groovy
package com.capgemini.fs.jenkins.plugins.deploy

import com.cloudbees.groovy.cps.NonCPS
import com.capgemini.fs.jenkins.plugins.bundle.Bundle

import java.util.logging.Logger

import static com.capgemini.fs.jenkins.plugins.proven.ProvenResource.dictionaryGavFor
import static com.capgemini.fs.jenkins.plugins.proven.ProvenResource.targetsDescriptorFor

class CheckDeployment {

	static Logger logger = Logger.getLogger('com.capgemini.fs.jenkins.plugins.deploy.CheckDeploy')

	static checkItemsToCheck(def itemsToCheck) {
		if (!itemsToCheck) {
			throw new Exception("le parametre 'itemToCheck' est obligatoire !")
		}

		if (!(
				(itemsToCheck.urls?.size() > 0)	^ (itemsToCheck.path && !itemsToCheck.path.isEmpty())
		)) {
			throw new Exception("Soit il faut renseigner le paramètre 'itemsToCheck.urls', soit le paramètre 'itemsToCheck.path'")
		}

		if (itemsToCheck.urls?.size() > 0 && !itemsToCheck.timeout) {
			itemsToCheck.timeout = 60
		} else if (itemsToCheck.urls?.size() > 0 && itemsToCheck.timeout > 1800) {
			throw new Exception("le parametre 'timeout' doit être inférieur à 1800s !")
		}
	}

	static def checkFadiaDeployment(script, def pvnMatrix, Bundle bundle, String approId) {
		String dictionaryGAV = dictionaryGavFor bundle.getReleaseGAV(), pvnMatrix
		script.echo "Recherche du dictionnaire pour le GAV : ${dictionaryGAV}"
		def dictionary = script.provenDictionary.get {
			gav = "${dictionaryGAV}"
		}
		String appli = dictionary["fadia.appli"]
		String cadre = dictionary["fadia.cadre"]

		String pseudoBundleGAV = bundle.releaseGAV.replaceAll('\\.', '-').replaceAll(':', '-').replaceAll('_', '-')
		def pvnTargets = targetsDescriptorFor bundle.releaseGAV, pvnMatrix
		boolean checked = true
		for (int i=0; i<pvnTargets.size(); i++) {
			def target = pvnTargets[i]
			String result = null
			if (!approId)  approId = ".*"
			String installFilePattern = "^install.*${pseudoBundleGAV}-${approId}_${cadre}...\\.ok\$|^install.*${pseudoBundleGAV}-${approId}_${cadre}...\\.ok\\.gz\$"
			script.echo "installFilePattern: $installFilePattern"
			result = script.sh(
					script: """ ssh -o StrictHostKeyChecking=no -l xjenki ${target.host} "ls /sasech/x${appli}/pur/liv | { grep -E -i -w '${installFilePattern}' || true; }" """,
					returnStdout: true
			)
			script.echo "sasech install files: \n${result}"
			if (!result) {
				checked = false
				script.echo "Pour le ${bundle.releaseGAV} sur la machine ${target.host}, le déploiement fadia n'a pas marché !"
			}
		}
		if (!checked) throw new Exception("Pour le ${bundle.releaseGAV}, le déploiement fadia n'a pas marché !")
	}

	static def checkDeployment(script, def pvnMatrix, Bundle bundle) {
        String dictionaryGAV = dictionaryGavFor bundle.getReleaseGAV(), pvnMatrix
        script.echo "Recherche du dictionnaire pour le GAV : ${dictionaryGAV}"
        def dictionary = script.provenDictionary.get {
            gav = "${dictionaryGAV}"
        }
        script.echo "ic.deploy.itemstocheck.urls:    ${dictionary['ic.deploy.itemstocheck.urls']} \n" +
                "ic.deploy.itemstocheck.timeout: ${dictionary['ic.deploy.itemstocheck.timeout']} \n" +
                "ic.deploy.itemstocheck.path:    ${dictionary['ic.deploy.itemstocheck.path']} \n"

        def itemsToCheck = [:]
        itemsToCheck['urls']       = dictionary["ic.deploy.itemstocheck.urls"]?.replaceAll("\\s",'')?.split(',')
        itemsToCheck['timeout']    = dictionary["ic.deploy.itemstocheck.timeout"] as int
        itemsToCheck['path']       = dictionary["ic.deploy.itemstocheck.path"]

        // Si aucun des paramètres indispensables n'est présent, alors aucune vérification ne sera faite
        if (!itemsToCheck.urls && !itemsToCheck.path) {
            script.echo "Warning! Aucune verifiaction post-deploiement ne sera faite ! \n" +
                    "En effet aucun des paramètres indispensables n'est présent dans le dictionnaire: \n" +
                    "  - ic.deploy.itemstocheck.urls \n" +
                    "  - ic.deploy.itemstocheck.path \n"
        } else {
            checkDeployment(script, pvnMatrix, bundle, itemsToCheck)
        }
    }

    static def checkDeployment(script, def pvnMatrix, Bundle bundle, def itemsToCheck) {
        checkItemsToCheck(itemsToCheck)
        if (itemsToCheck.urls) {
            checkDeployHttp(script, bundle.releaseVersion, itemsToCheck)
        } else if (itemsToCheck.path) {
            def pvnTargets = targetsDescriptorFor bundle.releaseGAV, pvnMatrix
            checkDeployFile(script, bundle, pvnTargets, itemsToCheck)
        }
    }

	// Ne pas changer la boucle for, ni ajouter @NonCPS, ne marche pas avec le timeout
	private static def checkDeployHttp(script, String releaseVersion, def itemsToCheck) {
		String deployedVersion
		URL url
		boolean checked
		for (int i = 0; i < itemsToCheck.urls.size(); i++) {
			url = new URL(itemsToCheck.urls[i])
			deployedVersion = ""
			checked = false
			script.timeout(time: itemsToCheck.timeout, unit: 'SECONDS') {
				while (!checked) {
					script.echo "Verification version a l'url ${url}"
					try {
						deployedVersion = url.getText(connectTimeout: 1000, readTimeout: 1000, useCaches: false, allowUserInteraction: false)
					} catch (Exception e) {
						script.echo "Failed connection ${e.getMessage()}"
					}
					script.echo "Version deployee : ${deployedVersion}"
					if (deployedVersion) {
						checked = true
					} else {
						sleep 10 // seconds
					}
				}
			}
			if (deployedVersion?.contains(releaseVersion)) {
				script.echo "Serveur UP et version OK ${releaseVersion}"
			} else {
				throw new Exception("Mauvaise version deployee ou serveur KO !")
			}
		}
	}

    @NonCPS
	private static def checkDeployFile(script, Bundle bundle, def pvnTargets, def itemsToCheck) {
		String deployedVersion
		boolean isDirectory
		String path
		for (def target : pvnTargets) {
			path = itemsToCheck.path
			script.echo "Verification de la version ${bundle.releaseVersion} sur la machine ${target.host} dasn le fichier ${path}"

			isDirectory = script.sh (
					script: """ ssh -o StrictHostKeyChecking=no -l xjenki ${target.host} "test -d ${path}" """,
					returnStatus: true
			) == 0
			if (isDirectory) path += "/${bundle.artifactId}.version"
			deployedVersion = script.sh (
					script: """ ssh -o StrictHostKeyChecking=no -l xjenki ${target.host} "cat ${path}" """,
					returnStdout: true
			)
			script.echo "deployedVersion: ${deployedVersion}"

			if (deployedVersion?.contains(bundle.releaseVersion)) {
				script.echo "Version OK"
			} else {
				throw new Exception("Mauvaise version deployee !")
			}
		}
	}
}