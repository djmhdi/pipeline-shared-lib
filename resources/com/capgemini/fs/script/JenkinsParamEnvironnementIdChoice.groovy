/**
 * Script pour generer le parametre jenkins contenant la liste des environnements present dans la configuration JSON
 * utilise par les pipelines livraison-fab et livraison-prod
 * via le plugin Active Choices Plug-in : https://wiki.jenkins-ci.org/display/JENKINS/Active+Choices+Plugin
 *
 * Parametrage :
 * utilisation d'un parametre de type 'Active Choices Reactive Parameter' en mode 'Single Select'
 * parametres du script JSON : configuration au format JSON
 */

try{
    def slurper = new groovy.json.JsonSlurper()
    def mapMatrix= slurper.parseText(JSON)
    List<String> listEnv = []
    for(def key in mapMatrix.keySet()){
        listEnv.add(key)
    }
    return listEnv
}catch(e){
    return ["[ERROR] " + e.getMessage(), JSON]
}