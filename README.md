## Jenkins Shared Library : DSL meant as an accelerator to build jenkins pipeline ##
** For maven projects mostly **

https://jenkins.io/doc/book/pipeline/shared-libraries/
https://github.com/fabric8io/jenkins-pipeline-library/

### DSL vocabulary
- release : set versions release, mvn build, tag, deploy set next versions snapshot
- notify : send mail, slack, teams...
- askPromotion : pause pipeline. Is the artifact ready for promotion ?
- promote : from a development nexus repo to a production nexus repo
- measureQuality : quality measures (Sonar via Maven or else)
- verifyQuality : If Sonar, callback from quality gate ?
- deploy : deploy an artifact on a node (abstract layer - what techno ?)
- askDeployment : pause pipeline - deploy validation
- verifyDeploymennt : deployment validation (one way validation - HTTP call ?)
- changelog : build changelog and commit in source control tool
- publishVersion : publish promoted version in a product dashboard



#### Memory Helper
Download from or to artifactory

---

        def downloadSpec = """{"files":[{
		"pattern": "<my-project>/1.0.5-7/(*).zip",					   
		"target": "pipeline-scripts.zip",
		"flat":"true"}]}"""
						
		def server = Artifactory.server "artifactory-ID"
		server.download(downloadSpec)
		
---

#### Tips
Abort stucked thread : Jenkins.instance.getItemByFullName("JobName").getBuildByNumber(JobNumber).finish(hudson.model.Result.ABORTED, new java.io.IOException("Aborting build"));
