https://jenkins.io/doc/book/pipeline/shared-libraries/

https://github.com/fabric8io/jenkins-pipeline-library/


Télécharger depuis ou vers Artifactory

---

        def downloadSpec = """{"files":[{
		"pattern": "RC/fr/pe/echd/service/ds013/ds013-pfe-batch/1.0.5-7/(*).zip",					   
		"target": "pipeline-scripts.zip",
		"flat":"true"}]}"""
						
		def server = Artifactory.server "artifactory-pe"
		server.download(downloadSpec)
		
---

Abort stucked thread : Jenkins.instance.getItemByFullName("JobName").getBuildByNumber(JobNumber).finish(hudson.model.Result.ABORTED, new java.io.IOException("Aborting build"));

Go to Jenkins slave's machine slzqv8
sudo ypicpe
cd /applis/ypicpe/pur/jsyt015/trt/1
jenkins_slave.ksh stop
jenkins_slave.ksh start