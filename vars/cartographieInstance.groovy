#!/usr/bin/groovy

/**
 *
 * @param body
 *        - INSTANCES instances à explorer, valeurs séparées par un ";" ou intervalle de valeurs séparées par un "-"
 *        - NOAUTH API interrogeable sans authentification
  * @return
 */

String[] jobTypes
String reportFileName

def call(body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    pipeline {
        agent none

        environment {
            APIPATH_FIND_JOBS = "/api/json?depth=2&pretty=true"
            JENKINS_HOST = "http://jenkins.pic-mutualisee.pole-emploi.intra"
            GITLAB_CREDENTIAL_ID = "gitlab_private_token"
            provenUser = "${env['PROVEN_user']}"
            provenPassword = "${env['PROVEN_password']}"

        }

        tools {
            maven "MAVEN_3_5_0_HOME"
            jdk "JAVA_1_8_0_66_HOME"
        }

        options {
            buildDiscarder(logRotator(numToKeepStr: '10'))
            disableConcurrentBuilds()
        }

        stages {
            stage('Cartographie') {
                agent {
                    label 'maven'
                }
                steps {
                    script {
                        String authString = ""
                        def instances = this.computeInstancesList(config.INSTANCES)

                        echo "Liste des instances à explorer : " + instances.join("+")

                        jobTypes = []
                        jobTypes.add("org.jenkinsci.plugins.workflow.job.WorkflowJob")
                        jobTypes.add("hudson.maven.MavenModuleSet")
                        jobTypes.add("org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject")

                        if (!config.NOAUTH) {
                            def result = input message: "Auth Jenkins - Saisissez votre token API", ok: 'Ok !',
                                    parameters: [[$class: 'TextParameterDefinition', defaultValue: "", description: 'Login Neptune', name: 'jenkinsUser'],
                                                 [$class: 'PasswordParameterDefinition', description: 'Mot de passe Neptune', name: 'jenkinsPassword']]
                            String jenkinsUser = result["jenkinsUser"]
                            String jenkinsPwd = result["jenkinsPassword"]
                            authString = "${jenkinsUser}:${jenkinsPwd}".getBytes().encodeBase64().toString()
                        }

                        //lecture des paramètres de deploiement
                        String buildQuery = env.BUILD_URL.replace("http://", "")
                        String[] urlSegments = buildQuery.split("/")

                        //reportFileName = "report-carto-${urlSegments[1]}.html"
                        reportFileName = "report-carto-instances-pic.html"

                        report = '<!doctype html><html lang="fr">'
                        report <<= '<head><meta charset="utf-8" />'
                        // report <<= "<title>Report PIC ${urlSegments[1]}</title>"
                        report <<= "<title>Cartographie des PIC</title>"
                        report <<= '</head><body>'

                        def jobsInfos
                        String jobsProperties

                        String requestUrl, bgColor
                        report = "<table style='border-collapse: collapse;border: 1px solid black;padding: 3px'><tr>"
                        int cpt = 0
                        for (String instance in instances) {
                            requestUrl = "${JENKINS_HOST}/jmyt${instance}${env.APIPATH_FIND_JOBS}"
                            echo "Tentative de connexion sur l'URL : ${requestUrl}"
                            if (config.NOAUTH) {
                                jobsProperties = new URL(requestUrl).getText(requestProperties: [Accept: 'application/json'])
                            } else {
                                jobsProperties = new URL(requestUrl).getText(requestProperties: [Authorization: "Basic $authString", Accept: 'application/json'])
                            }
                            jobsInfos = json.parseFromText(jobsProperties)

                            if (cpt % 2 == 0) {
                                bgColor = "white"
                            } else {
                                bgColor = "#EDEDED"
                            }

                            report <<= "<td style='width: 250px; vertical-align:top;padding-left: 5px;padding-right: 5px;background-color: ${bgColor}" + "'><h2 style='text-align: center'>Instances ${instance}</h2>"
                            report <<= this.loadReportRecursively(jobsInfos.jobs, "")
                            report <<= "</td>"
                            cpt = cpt + 1
                        }
                        report <<= "</tr></table>"
                        report <<= "</body></html>"
                    }
                    writeFile(file: reportFileName, text: report.toString(), encoding: "UTF-8")

                    publishHTML(target: [
                            reportName  : 'Cartographie Instance',
                            reportDir   : '.',
                            reportFiles : reportFileName,
                            allowMissing: false
                    ])
                }
            }
        }
    }
}

@NonCPS
String loadReportRecursively(def jobList, def folder) {
    String jobsReport = "<div>"
    for (def job in jobList) {
        if (job._class == 'com.cloudbees.hudson.plugins.folder.Folder' && (job.jobs != null)) {
            if (job.jobs.size() > 0) {
                if (folder != "") {
                    folder <<= "/"
                }

                folder <<= job.name
                jobsReport <<= this.loadReportRecursively(job.jobs, folder)
            }
        } else if (jobTypes.contains(job._class)) {
            boolean isTesiCompliant = (job.name =~ /[dn|dd|da|ds|pn|ex][0-9]{3}.*/)

            jobsReport <<= "${folder}/" + '<span style="'
            if (!isTesiCompliant) {
                jobsReport <<= "color: red"
            } else {
                jobsReport <<= "color: green"
            }
            jobsReport <<= '">'
            jobsReport <<= "${job.name}</span><br />"
        }
    }
    jobsReport <<= "</div>"
    return jobsReport
}

@NonCPS
def computeInstancesList(String instances) {
    String[] entries = instances.split(";")
    def cleanedInstances = []
    String[] range

    for (String entry in entries) {
        if (entry.contains("-")) {
            range = entry.split("-")

            int minRange = range[0] as Integer
            int maxRange = range[1] as Integer
            int scale = maxRange - minRange

            String newEntry
            for (int i = 0; i <= scale; i++) {
                newEntry = String.valueOf(minRange + i).padLeft(3, "0")
                cleanedInstances.add(newEntry)
            }
        } else {
            cleanedInstances.add(entry.padLeft(3, "0"))
        }
    }

    return cleanedInstances
}
