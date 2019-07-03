/**
 * Script pour generer le parametre jenkins contenant la configuration au format JSON
 * utilise par les pipelines livraison-fab et livraison-prod
 * via le plugin Active Choices Plug-in : https://wiki.jenkins-ci.org/display/JENKINS/Active+Choices+Plugin
 *
 * Parametrage du parametre :
 * utilisation d'un parametre de type 'Active Choices Reactive Reference Parameter' en mode 'Formatted Hidden HTML'
 */
import org.apache.commons.lang.StringEscapeUtils

String htmlStr
try{
    String jsonConfig =  StringEscapeUtils.escapeHtml(new URL(CONFIG_URL).getText(requestProperties: [Accept: 'application/json', 'PRIVATE-TOKEN': PRIVATE_TOKEN]))
    htmlStr = "<input name='value' type='text' class='setting-input' value=\"$jsonConfig\"/>"
}catch (err){
    htmlStr = """
    - CONFIG_URL:$CONFIG_URL
    - PRIVATE-TOKEN:$PRIVATE_TOKEN
<p style='color:#FF0000'>[ERROR]${err.getMessage()}</p>"""
}
return htmlStr