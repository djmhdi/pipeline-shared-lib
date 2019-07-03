#!/usr/bin/groovy
package fr.pe.jenkins.plugins.proven

import org.junit.Test
import fr.pe.jenkins.plugins.proven.ProvenResource
import fr.pe.jenkins.plugins.bundle.Bundle
import static groovy.test.GroovyAssert.*
import static  fr.pe.jenkins.plugins.proven.ProvenResource.*

class ProvenResourceTest {

    def matrix = [matrixElements: [
            [target: "apache.gedfa.slz9pn:sfjg13bb", bundleGAV: "fr.pe.moye.progiciel.pr001:pr001-test:1.3.455-20"],
            [target: "apache.pr001.slzq9f:apfrx337", bundleGAV: "fr.pe.moye.progiciel.pr001:pr001-test:1.3.455-20"],
            [target: "tomcat.coli2.slzkor:katrt386", bundleGAV: "fr.pe.moye.progiciel.pr008:pr008-colibri-nuxeo:1.3.45-20"],
            [target: "tomcat.coli2.slzkh9:katrt386", bundleGAV: "fr.pe.moye.progiciel.pr008:pr008-colibri-nuxeo:1.3.45-20"],
            [target: "tomcat.coli2.slzkm6:katrt386", bundleGAV: "fr.pe.moye.progiciel.pr008:pr008-colibri-nuxeo:1.3.45-20"],
            [target: "weblogic.pinot.slzkag:wlsqt250", bundleGAV: "fr.pe.intranet:pinot-intramrs-tapestry:16.4.9-17"],
            [target: "host.R7:slzkm6", bundleGAV: "fr.pe.moye.progiciel.pr008:pr008-colibri-redis:1.0.3-7"]
    ]]

    @Test
    void hostnamesForTest() {

        def hosts = hostnamesFor('fr.pe.moye.progiciel.pr008:pr008-colibri-nuxeo:1.3.45-20', matrix)
        assertEquals("oh shit !", hosts, ["slzkor", "slzkh9", "slzkm6"])
    }

    @Test
    void targetsDescriptorForTest() {

        def targets = targetsDescriptorFor('fr.pe.moye.progiciel.pr001:pr001-test:1.3.455-2', matrix)
        assertEquals("oh shit !", targets, [["type":"apache", "host":"slz9pn", "owner":"sfjg13bb", "numInstance":"3bb"], ["type":"apache", "host":"slzq9f", "owner":"apfrx337", "numInstance":"337"]])
    }
}