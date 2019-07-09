/**
 * Script pour afficher la liste des bundles présent dans la matrice et permettre a l'utilisateur de selectionner les bundles qu'il veut livrer
 * utilise par les pipelines livraison-fab et livraison-prod
 * via le plugin Active Choices Plug-in : https://wiki.jenkins-ci.org/display/JENKINS/Active+Choices+Plugin
 *
 * Parametrage :
 * utilisation d'un parametre de type 'Active Choices Reactive Reference Parameter' en mode 'Formatted HTML'
 * parametres du script :
 *      - JSON : configuration au format JSON
 *      - ID : identifiant de l'environnement
 *      - PROVEN_user : login proven de jenkins (par defaut : $PROVEN_user)
 *      - PROVEN_password : password proven de jenkins (par defaut : $PROVEN_password)
 *      - PROVEN_url : url de proven pour la récupération de la matrice (par defaut : $PROVEN_url)
 */

try{
    def config = new groovy.json.JsonSlurper().parseText(JSON)
    String matrixGAV = config[ID].matrix
    String matrixPath = matrixGAV.replaceAll(":", "/").replaceAll("\\.", "/")

    String userpass = PROVEN_user + ":" + PROVEN_password;
    String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes());
    String urlMatrix = PROVEN_url + "/proven-server/matrix2/" + matrixPath
    String Matrix_JSON = new URL(urlMatrix).getText(requestProperties: [Accept: 'application/json', 'Authorization': basicAuth])

    def matrix= new groovy.json.JsonSlurper().parseText(Matrix_JSON)
    def lineSeparator = "<br>"
    def bundleMap = [:]
    for(def element in matrix.provisionings){

        def gavSplitted = element.bundleCompactName.split(':')
        String bundlePath = gavSplitted[0].replaceAll("\\.", "/") + "/" + gavSplitted[1] + "/" + gavSplitted[2]
        String urlBundle = PROVEN_url + "/proven-server/bundle/" + bundlePath
        String Bundle_JSON = new URL(urlBundle).getText(requestProperties: [Accept: 'application/json', 'Authorization': basicAuth])
        def bundle= new groovy.json.JsonSlurper().parseText(Bundle_JSON)
        def artifactUrl = bundle.descriptor.artifactaddress.url
        String extension = artifactUrl.substring(artifactUrl.lastIndexOf(".")+1);

        def bundleElement = ['target': element.deliverableElementCompactName, 'dictionary':element.dictionaryCompactName, 'json':Bundle_JSON, 'extension':extension]
        def gavp = element.bundleCompactName+":$extension"
        if(bundleMap[gavp]){
            bundleMap[gavp].add(bundleElement)
        }else{
            bundleMap[gavp] = [bundleElement]
        }
    }

    String matrixString = "<p STYLE='color:red;'><b>Matrix $ID</b> : ${matrix.group}:${matrix.name}:${matrix.version}</p>"
    for(def gavp in bundleMap.keySet()){
        def gavpSplitted = gavp.split(':')
        String groupId = gavpSplitted[0]
        String artifactId = gavpSplitted[1]
        String version = gavpSplitted[2]
        String packaging = gavpSplitted[3]
        matrixString += "<input type='checkbox' name='value' id=\"${groupId}:${artifactId}\" json=\"${gavp}\" >"
        matrixString += "<label class=\"attach-previous\"><b>Bundle : </b>" + groupId + " : " + artifactId + " : "
        matrixString += "<input type='text' name='version' onchange=\"javascript:document.getElementById('${groupId}:${artifactId}').setAttribute('json', '${groupId}:${artifactId}:'+ this.value + ':${packaging}')\" value=\"${version}\">"
        matrixString += "</label>"
        matrixString += "<table BORDER CELLSPACING=0 CELLPADDING=2 STYLE='font-size:13px'>"
        matrixString += "<tr BGCOLOR='#C0C0C0'>"
        matrixString += "<th WIDTH=500 ALIGN='left' STYLE='padding-left:10px'>Dictionary</th>"
        matrixString += "<th WIDTH=300 ALIGN='left' STYLE='padding-left:10px'>Target</th>"
        matrixString += "</tr>"
        bundleMap[gavp].each{value ->
            matrixString +=  "<tr><td>" + value.dictionary + "</td><td>" + value.target + "</td></tr>"
        }
        matrixString += "</table><br>"
    }
    return matrixString
}catch(e){
    return """<pre STYLE='color:red;'>
    - ID:$ID
    - JSON:$JSON
    - [ERROR] ${e.getMessage()}</pre>"""
}