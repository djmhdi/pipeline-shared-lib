#!/usr/bin/groovy
package fr.pe.jenkins.plugins.notification

enum NotificationDataKeys {
    PROJECT_NAME('projectName'),
    DESCRIPTION('description'),
    MATRICE('matrice'),
    CADRE('cadre'),
    BUNDLE('bundle'),
    BUNDLES('bundles'),
    DELIVERY('delivery'),
    CURRENT_BUILD('currentBuild'),
    PROMOTION_LEVEL('promotionLevel'),
    SONAR('sonar'),
    ENV('env'),
    COMPONENT('component'),
    SIGNATURE('signature'),
    TITLE('title'),
    DEPLOYE('deploye')

    NotificationDataKeys(String datakey) {
        this.key = datakey
    }

    private final String key

    public String toString() {
        return this.key
    }
}
