FROM docker-virtual.artefact-repo.pole-emploi.intra/cloudbees/java-build-tools

USER root
RUN mkdir -p /root/.ssh /donapp/ypicpe/p00/dat/maven/global
RUN touch /root/.ssh/cle_jenkins.rsa

RUN echo "Host git-scm.pole-emploi.intra \n IdentityFile /root/.ssh/cle_jenkins.rsa \n  StrictHostKeyChecking no" > /root/.ssh/config
RUN chmod -R 600 /root/.ssh/

RUN wget -O /usr/lib/provenclient-rest-1.6.0-131-all.jar "http://artefact-repo.pole-emploi.intra/artifactory/Prod/fr/pe/pkg/provenclient/provenclient-rest/1.6.0-131/provenclient-rest-1.6.0-131-all.jar"
