#!/usr/bin/groovy

def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    // now build, based on the configuration provided
    echo "================================================"
    echo config.bundle.toString();
    echo "================================================"
    
    checkRequirements()
}

def checkRequirements(){
    String provenJar = "${env.PROVEN_JAR}"
    if (!provenJar.trim()) {
        throw new Exception("L'URL du jar proven n'est pas définie !")
    } else {
        echo "-- ProvenJar :" + provenJar
    }
	if (!env.PROVEN_CREDENTIAL_ID?.trim()) {
        throw new Exception("La variable d'environnement PROVEN_CREDENTIAL_ID n'est pas définie !")
    } else {
        echo "-- PROVEN_CREDENTIAL_ID :" + env.PROVEN_CREDENTIAL_ID
    }
}