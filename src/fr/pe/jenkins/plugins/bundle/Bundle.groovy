#!/usr/bin/groovy
package fr.pe.jenkins.plugins.bundle

class Bundle implements Serializable {
    String groupId, artifactId, releaseVersion, nextDevelopmentVersion, packaging, changelog
    boolean unzipArtifact = false
	
    Bundle(String groupId, String artifactId, String releaseVersion, String nextDevelopmentVersion, String packaging) {
		this.groupId = groupId
        this.artifactId = artifactId
		this.releaseVersion = releaseVersion
		this.nextDevelopmentVersion = nextDevelopmentVersion
        this.packaging = packaging
    }

    Bundle(String groupId, String artifactId, String releaseVersion, String nextDevelopmentVersion, String packaging, boolean unzip) {
        this.groupId = groupId
        this.artifactId = artifactId
        this.releaseVersion = releaseVersion
        this.nextDevelopmentVersion = nextDevelopmentVersion
        this.packaging = packaging
        this.unzipArtifact = unzip
    }
	
    def String getReleasePath() {
        return "${this.groupId.replace('.', '/')}/${this.artifactId}/${this.releaseVersion}"
    }

    private String getArtifactName() {
        return new StringBuilder(this.artifactId).append("-").append(releaseVersion).append(".").append(packaging).toString();
    }

    public String getDeploymentPath() {
        return new StringBuilder(this.groupId.replace(".", "/")).append("/").append(this.artifactId).append("/").append(this.releaseVersion).
                append("/").append(getArtifactName()).toString();
    }

    public String getReleasePomURL(String repoBaseUrl) {
        return repoBaseUrl + "/" + this.releasePath + "/${this.artifactId}-${this.releaseVersion}.pom"
    }

    public String getReleaseArchiveURL(String repositoryURL) {
        return new StringBuilder(repositoryURL).append("/")
                .append(this.releasePath).append("/")
                .append(this.releaseArchiveName.toString());
    }

    public String getReleaseArchiveURL(String repositoryBaseURL, String repositoryKey) {
        return new StringBuilder(repositoryBaseURL).append("/")
                .append(repositoryKey).append("/")
                .append(this.releasePath).append("/")
                .append(this.releaseArchiveName.toString());
    }
	
	public String getReleaseArchiveName(){
		return "${this.artifactId}-${this.releaseVersion}.${this.packaging}"
	}
    
	public String getReleaseGAV() {
		return this.groupId + ':' + this.artifactId + ':'  +this.releaseVersion
	}
	
	public String getNextDevelopmentGAV() {
		return "${this.groupId}:${this.artifactId}:${this.nextDevelopmentVersion}"
	}

    @Override
    String toString() {
        return this.releaseGAV
    }
}
