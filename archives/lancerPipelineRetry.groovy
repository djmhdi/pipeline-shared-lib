#!/usr/bin/groovy
/**
 * def pipelineParameters = []
 pipelineParameters.add(string(name: 'CADRE', value: 'IQLB'))
 pipelineParameters.add(string(name: 'BUNDLE_GAV', value: "fr.pe.test:test:1.0.0"))
 */
import static com.capgemini.fs.jenkins.plugins.bundle.BundleFactory.*
import com.capgemini.fs.jenkins.plugins.util.*
import com.capgemini.fs.jenkins.plugins.notification.*

/**
 * Permet de lancer un job externe de type pipeline en lui passant une liste de paramètres
 * @param body
 * def livparameters = []
 * livparameters.add(string(name: 'CADRE', value: 'TIC'))
 * livparameters.add(string(name: 'BUNDLE_GAV', value: "fr.pe.test:test:1.0.0"))
 */
def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    check(config)
    echo "-------- Pipeline parameters : " + config.pipelineParameters.join(",")
    boolean isSuccessful = false
    while (!isSuccessful) {
        try {
            build job: config.cheminPipeline,
                    propagate: true,
                    parameters: config.pipelineParameters
            isSuccessful = true
        } catch (err) {
            def w = new StringWriter()
            err.printStackTrace(new PrintWriter(w))
            echo "[MESSAGE]:${err.message}${System.getProperty("line.separator")}${w.toString()}}"
            w = null
            timeout (time: config.retryTimeout, unit: 'HOURS') {
                input message:"${config.cheminPipeline} en Echec !", ok: "Relancer ?"
            }

        }
    }
}

def check(def config) {
    if (!config.cheminPipeline) {
        throw new Exception("le parametre 'cheminPipeline' est obligatoire !")
    }

    if (!config.pipelineParameters) {
        config.pipelineParameters = []
    }

    if (!config.retryTimeout) {
        config.retryTimeout = 1
    }
}
