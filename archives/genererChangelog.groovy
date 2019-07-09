#!/usr/bin/groovy
import com.capgemini.fs.jenkins.plugins.util.*
import com.capgemini.fs.jenkins.plugins.bundle.Bundle

import static com.capgemini.fs.jenkins.plugins.restart.Restart.restart

def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    check(config)

    genererChangelog(config)
}

def genererChangelog(config) {

    echo "============== Création du fichier de changelog ========================="
    def changelogPath = "${pwd()}/CHANGELOG.md"
    echo "--- changelogPath : ${changelogPath}"
            
    echo "Generation du fichier ${changelogPath} pour la version: ${config.range}"
    def ranges = [:]
    def completeFile = false
    if (config.range == null) {
        config.range = 'snapshot'
    }
    switch (config.range) {
        case 'concat':
            if (fileExists(changelogPath)) {
                ranges["${config.bundle.releaseVersion} - " + new Date().format("yyyy-MM-dd' 'HH:mm:ss'Z'", TimeZone.getTimeZone("UTC"))] = getSnapshotRange()
                completeFile = true
            } else {
                ranges = getAllRanges(config.bundle)
            }
            break
        case 'all':
            ranges = getAllRanges(config.bundle)
            break
        case 'lastRelease':
            ranges = getReleaseRange(config.bundle)
            break
        case 'snapshot':
            ranges["${config.bundle.releaseVersion} - " + new Date().format("yyyy-MM-dd' 'HH:mm:ss'Z'", TimeZone.getTimeZone("UTC"))] = getSnapshotRange()
            break
        default:
            ranges["${config.range}"] = "${config.range}"
    }
    echo "Recuperation de la conf du changelog"
    def configs = convertJsonConfig(config.changelogConfig)
    def commits = []
    def typeCommits = []
    ranges.each { key, range ->
        echo "Traitement de la version: ${key}"
        commits.add("# ${key}")
        commits.add("")
        for(conf in configs) {
            typeCommits = getCommitsFromRange("${range}", conf)
            if (typeCommits.size() > 0) {
                commits.addAll(typeCommits)
                commits.add("")
            }
        }
    }
    if (fileExists(changelogPath) && !completeFile) {
        echo "Le changelog existe déjà, on le supprime avant de générer le nouveau"
        sh "rm ${changelogPath}"
    }
    echo "Ecriture du fichier"
    String changelog = "${commits.join('\n')}"
    if (completeFile && fileExists(changelogPath)) {
        sh "echo \'${changelog}\' | cat - ${changelogPath} > temp && mv temp ${changelogPath}"
    } else {
         writeFile (file: "${changelogPath}", text: changelog, encoding: 'UTF-8')
    }
    // Corrige le pb jenkins d'encodage
    def charset = sh (script: "file -i ${changelogPath} | grep -oP 'charset=([a-zA-Z\\-0-9]*)' | sed  's/charset=//'",returnStdout: true).trim()
    echo "Encoding ${charset}"
    if (charset != 'utf-8') {
        sh "iconv -f ISO-8859-1 -t UTF-8 ${changelogPath} -o ${changelogPath}"
    }
    
    if (config.archive) {
        echo "--- archive changelog"
        archiveArtifacts artifacts: "CHANGELOG.md"
    }
    
    if (config.gitpush) {
        echo "--- mise à jour sous git"
        sh "git add CHANGELOG.md && git commit -m \"[Jenkins] Mise à jour du changelog pour la version ${config.bundle.releaseVersion}\" && git push"
    }
    if (config.saveToBundle) {
        echo "Mise à jour du bundle"
        config.bundle.changelog = changelog
    }
}


def check(def config) {
    if (!config.node) {
        config.node = JenkinsNodes.DEFAULT.label
    }
}


def convertJsonConfig(def jsonConf) {
    if (jsonConf == null) {
        jsonConf = "[{\"prefix\":\"\",\"titre\":\"Tous les commits:\"}]"
    } else if (jsonConf == 'pole-emploi') {
        jsonConf = "[{\"prefix\":\"Feat\",\"titre\":\"Fonctionnalitées:\",\"features\":\"true\"},{\"prefix\":\"Fix\",\"titre\":\"Correctifs:\",\"features\":\"true\"},{\"prefix\":\"Refacto\",\"titre\":\"Réusinage:\"},{\"prefix\":\"Style\",\"titre\":\"Style:\"},{\"prefix\":\"Docs\",\"titre\":\"Documentation:\"},{\"prefix\":\"Chore\",\"titre\":\"Evolutions techniques:\"},{\"prefix\":\"Test\",\"titre\":\"Tests:\"}]"
    }
    return json.parseFromText(jsonConf)
}

def getCommitsFromRange(def range, def config) {
    def gitCmd = "git log ${range} -i --format='%s' --encoding='UTF-8' --cherry-pick --no-merges --grep=^${config.prefix}"
    def com = sh (script: "${gitCmd}",returnStdout: true).tokenize('\n')
    com.unique()
    if (config.prefix == "") {
        com = treatCommitsByDefault(com)
    } else {
        com = treatCommitsWithConfig(com, config.features)
    }
    if (com.size() > 0) {
        com.add(0, "")
        com.add(0, "## ${config.titre}")
    }
    return com
}

def treatCommitsByDefault(def commits) {
    def lines = []
    for (line in commits) {
        lines.add("* ${line}")
    }
    lines.sort()
    return lines
}

def treatCommitsWithConfig(def commits, def features) {
    def lines = []
    if (features != null && features == "true") {
        def foncMap = [:]
        for (line in commits) {
            def fonc = (line =~ /\(([a-zA-Z]+ {0,1}[0-9]*)\) {0,1}:/)
            if (fonc.size() > 0 && fonc[0].size() > 1) {
                def foncs = foncMap.get(fonc[0][1])
                if (foncs == null || foncs.size() ==0) {
                    foncs = []
                    foncMap.put(fonc[0][1], foncs)
                }
                foncs.add(removeCommitPrefix(line, '    * '))
            } else {
                lines.add(removeCommitPrefix(line, '* '))
            }
        }
        lines.sort()
        for (cle in foncMap.keySet()) {
            lines.add("")
            lines.add("* **${cle}**")
            def foncs = foncMap.get(cle)
            foncs.sort()
            lines.addAll(foncs)
        }
    } else {
        for (line in commits) {
            lines.add(removeCommitPrefix(line, '* '))
        }
        lines.sort()
    }
    return lines
}

def removeCommitPrefix(def commit, def mdPrefix) {
    return mdPrefix + commit.substring(commit.indexOf(':') +1).trim()
}

def getSnapshotRange() {
    def lastTag = ''
    try {
        lastTag = sh (script: 'git describe --tags --abbrev=0',returnStdout: true).trim()
        echo "Dernier tag: ${lastTag}"
        lastTag = "${lastTag}.."
    } catch(exception) {
        echo "Pas de tag trouvé ${exception}"
    }
    return lastTag
}

def getReleaseRange(def bundle) {
    sh 'git fetch'
    def tags = sh (script: "git for-each-ref --sort=-taggerdate --format='%(tag)' --count=2 refs/tags",returnStdout: true).tokenize('\n')
    def releaseRange = [:]
    if (tags.size() > 0) {
        def dateTag = sh (script: "git for-each-ref --sort=-taggerdate --format='%(taggerdate:iso8601)' --count=1 refs/tags",returnStdout: true)
        String rangeValue
        if (tags.size() == 1) {
            rangeValue = tags[0].replace("${bundle.artifactId}-",'')
        } else {
            rangeValue = "${tags[1]}..${tags[0]}"
        }
        releaseRange["${tags[0]} - ${dateTag}"] = rangeValue
    } else {
        releaseRange["${bundle.releaseVersion} - " + new Date()] = ''
    }
    return releaseRange
}

def getAllRanges(def bundle) {
    sh 'git fetch'
    def tags = sh (script: "git for-each-ref --sort=-taggerdate --format='%(tag)@@%(taggerdate:iso8601)' refs/tags",returnStdout: true).tokenize('\n')
    def allRanges = [:]
    def size = tags.size()
    if (size == 0) {
            allRanges["${bundle.releaseVersion} - " + new Date().format("yyyy-MM-dd' 'HH:mm:ss'Z'", TimeZone.getTimeZone("UTC")) ] = ''
    } else {
        allRanges["${bundle.releaseVersion.replace('-SNAPSHOT','')} - " + new Date().format("yyyy-MM-dd' 'HH:mm:ss'Z'", TimeZone.getTimeZone("UTC")) ] = tags[0].tokenize('@@').first() + '..'
        size--
        for (i = 0; i < size; i++) {
            allRanges["${tags[i].replace('@@', ' - ')}"] = tags[i + 1].tokenize('@@').first() + '..' + tags[i].tokenize('@@').first()
        }
        allRanges["${tags[size].replace('@@', ' - ')}"] = tags[size].tokenize('@@').first()
    }
    return allRanges
}
