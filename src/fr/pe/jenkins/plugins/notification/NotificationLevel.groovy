#!/usr/bin/groovy
package fr.pe.jenkins.plugins.notification

enum NotificationLevel implements Serializable {
    @Deprecated
    ALL('all'),
    // Ils sont classés dans le même ordre que Jenkins
    SUCCESS('SUCCESS'), UNSTABLE('UNSTABLE'), FAILURE('FAILURE'), NOT_BUILT('NOT_BUILT'), ABORTED('ABORTED')

    private static final def notifications = [:]

    static {
        NotificationLevel[] notifsTab = NotificationLevel.values()
        for (int i=0; i<notifsTab.size(); i++) {
            notifications[notifsTab[i].level] = notifsTab[i]
        }
    }

    // Cela semble inutile, mais pas du tout
    // Quand on ne connait pas son type d'entrée, cela permet d'être sur d'avoir son Enum.
    static NotificationLevel getFromLevel(NotificationLevel level) {
        return level
    }

    static NotificationLevel getFromLevel(String level) {
        return notifications[level]
    }

    private final String level

    NotificationLevel(String notificationLevel) {
        this.level = notificationLevel
    }

    String toString() {
        return this.level
    }
}