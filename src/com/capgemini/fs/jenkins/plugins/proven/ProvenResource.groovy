#!/usr/bin/groovy
package com.capgemini.fs.jenkins.plugins.proven

import java.util.logging.Logger
import com.capgemini.fs.jenkins.plugins.bundle.BundleFactory
import com.capgemini.fs.jenkins.plugins.bundle.Bundle
import com.cloudbees.groovy.cps.NonCPS

class ProvenResource {
	
	static Logger logger = Logger.getLogger('com.capgemini.fs.jenkins.plugins.proven.ProvenResource')

	@NonCPS
	static String dictionaryGavFor(def bundleGAV, def provenMatrix) {
		String dictionary = provenMatrix.matrixElements.findResult{ it.bundleGAV.startsWith("${bundleGAV}")?it.getDictionaryGAV():null }
//		def dictionary = null
//        for(def element in provenMatrix.matrixElements){
//            if(element.bundleGAV.startsWith("$bundle.groupId:$bundle.artifactId")){
//                dictionary = element.getDictionaryGAV()
//                break
//            }
//        }
        
		return dictionary
	}

    @NonCPS
	static def hostnamesFor(def bundleGAV, def provenMatrix) {
		def hostnames = []
//		return provenMatrix.matrixElements.findResult { it.bundleGAV.startsWith("$bundle.groupId:$bundle.artifactId")? it.getDictionaryGAV():null }
//		provenMatrix.matrixElements.each { it.bundleGAV.startsWith("$bundle.groupId:$bundle.artifactId")? hostnames.add(hostnameFor(it.getTarget())):null }
		for(def element in provenMatrix.matrixElements){
			if(element.bundleGAV.startsWith("${bundleGAV}")){
				hostnames.add(hostnameFor(element.target))
			}
		}
		return hostnames
	}

	@NonCPS
	static Bundle[] bundlesFromMatrix(def provenMatrix, String bundlePrefix) {
		def bundles = [] as Bundle[]
		for(def element in provenMatrix.matrixElements){
			if(element.bundleGAV.startsWith("${bundlePrefix}")){
                bundles.add(BundleFactory.createBundleFromGAV(element.bundleGAV))
			}
		}
		return bundles
	}

    @NonCPS
    static String[] bundlesGAVFromMatrix(def provenMatrix, String bundlePrefix) {
        def bundles = []
        for(def element in provenMatrix.matrixElements){
            if(element.bundleGAV.startsWith("${bundlePrefix}")){
                bundles.add(element.bundleGAV)
            }
        }
        return bundles
    }

    @NonCPS
	static String hostnameFor(def targetGAV){
	    String host
	    if (targetGAV.startsWith("host") || targetGAV.startsWith("temp")) {
	      host = targetGAV.substring(targetGAV.lastIndexOf(':') + 1)
	    } else { // if (targetGAV.startsWith("weblogic")) {
	      host = targetGAV.substring(targetGAV.lastIndexOf('.') + 1,targetGAV.lastIndexOf(':'))
//	    } else {
//	      host = targetGAV.substring(0, targetGAV.lastIndexOf(':')).substring(targetGAV.lastIndexOf('.') + 1)
	    }
	    return host
	}

	@NonCPS
	static def targetsDescriptorFor(def bundleGAV, def provenMatrix) {
		def targets = []
		for(def element in provenMatrix.matrixElements){
			if (bundleGAV == null || element.bundleGAV.startsWith("${bundleGAV}")) {
				def target = [:]
				target.type =  element.target.substring(0,element.target.indexOf('.'))
				target.host = hostnameFor(element.target)
				target.owner = ownerFor(element.target)

				// Calcul le numéro d'instance à partir du nom du owner
				if (target.owner){
					target.numInstance = target.owner.substring(5)
				}
				targets.add(target)
			}
		}
		return targets
	}

	@NonCPS
	static def targetsDescriptorFor(def provenMatrix) {
		return targetsDescriptorFor(null, provenMatrix)
	}

	@NonCPS
	static String ownerFor(def targetGAV){
		String owner
		if (targetGAV.startsWith("host") || targetGAV.startsWith("temp")) {
			owner = ''
		} else {
			owner = targetGAV.substring(targetGAV.lastIndexOf(':') + 1)
		}
		return owner
	}
}
