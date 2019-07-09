#!/usr/bin/groovy
import com.capgemini.fs.jenkins.plugins.notification.NotificationDataKeys
import com.capgemini.fs.jenkins.plugins.bundle.BundleFactory
import com.capgemini.fs.jenkins.plugins.util.*

def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    check(config)

    for (def bundleToPromote : config.bundles) {
        if (notAlreadyPromoted(bundleToPromote, config.repositoryDest)) {
            def artifactUrlDest = bundleToPromote.getReleaseArchiveURL(env[config.repositoryDest.envParameterName])
            promoteBundleInRepo(bundleToPromote, config.repositorySource, config.repositoryDest)

            proven.promoteBundle {
                bundle = bundleToPromote.releaseGAV
                url = artifactUrlDest
                promotionLevel = config.repositoryDest.label
            }
        }
    }
    updateMatrices(config.bundles, config.matrices)

    // On boucle sur les notifications et on y ajoute le resultat du deliver proven
    for (int i = 0; i < config.notifications.size(); i++) {
        config.notifications[i].appendData(NotificationDataKeys.BUNDLES.key, config.bundles)
        config.notifications[i].appendData(NotificationDataKeys.PROMOTION_LEVEL.key, config.repositoryDest.label)
    }

    notifier {
        notifications = config.notifications
    }
}

def check(def config) {
    if ((!config.bundle && !config.bundles) || (config.bundle && config.bundles)) {
        throw new Exception("le parametre 'bundles' est obligatoire ! (bundle est Deprecated)")
    }

    if (!config.projectName) {
        config.projectName = env.JOB_NAME
    }
    if (!config.bundles) {
        config.bundles = [config.bundle]
    }
    config.bundle = null
    if (!config.matrices) {
        config.matrices = []
    }

    if (!config.notifications) {
        config.notifications = []
    }
}

def notAlreadyPromoted(def bundle, def repository) {
    def pomUrl = bundle.getReleasePomURL(env[repository.envParameterName])
    try {

        sh(""" wget -O/dev/null -q ${pomUrl} && exit 1
                exit 0""")
    } catch (e) {
        echo "WARN : bundle ${bundle.releaseGAV} already promoted in ${repository.label} !"
        return false
    }
    return true
}

def promoteBundleInRepo(def bundleToPromote, def repoSource, def repoDest) {

    def urlRepoSource = env[repoSource.envParameterName]
    def urlRepoDest = env[repoDest.envParameterName]
    def pomUrl_Source = bundleToPromote.getReleasePomURL(urlRepoSource)
    def artifactUrl_Source = bundleToPromote.getReleaseArchiveURL(urlRepoSource)


    sh "curl -O $pomUrl_Source"
    sh "curl -O $artifactUrl_Source"
    sh "mvn deploy:deploy-file  -gs ${env['MAVEN_PE_SETTINGS']} -Durl=${urlRepoDest} -Dfile=${bundleToPromote.releaseArchiveName} -DgroupId=${bundleToPromote.groupId} -DartifactId=${bundleToPromote.artifactId} -Dversion=${bundleToPromote.releaseVersion} -Dpackaging=${bundleToPromote.packaging}"
}

def updateMatrices(def bundles, def matrices) {
    def bundlesGAV = BundleFactory.createListGAVFromBundle(bundles)
    for (def matrice in matrices) {
        echo "\n============== Update matrix: ${matrice} ========================="
        echo " -- newBundlesGAV : $bundlesGAV"
        proven.deltaUpdateBundleVersionInMatrix {
            gav = matrice
            deltas = bundlesGAV
        }
        echo "============================================================================="
    }
}