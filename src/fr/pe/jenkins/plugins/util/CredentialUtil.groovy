package fr.pe.jenkins.plugins.util

import com.cloudbees.plugins.credentials.Credentials
import com.cloudbees.plugins.credentials.CredentialsProvider
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials
import com.cloudbees.plugins.credentials.domains.Domain
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import jenkins.model.Jenkins;

class CredentialUtil {
    static def String addNewCredential(def login, def password){
        String id = 'proven_user_' + java.util.UUID.randomUUID().toString()
        Credentials c = (Credentials) new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, id, "tmp", login, password)
        SystemCredentialsProvider.getInstance().getStore().addCredentials(Domain.global(), c)
        return id
    }

    static def removeCredential(String credentialId){
        def creds = CredentialsProvider.lookupCredentials(StandardUsernameCredentials.class, Jenkins.instance, null, null)
        for (c in creds) {
            if(c.id == credentialId){
                SystemCredentialsProvider.getInstance().getStore().removeCredentials(Domain.global(), c)
            }
        }
    }
}
