#!/usr/bin/groovy
package com.capgemini.fs.jenkins.plugins.notification

import com.cloudbees.groovy.cps.NonCPS

enum NotificationProtocol implements Serializable {
	MAIL('mail'), SLACK('slack'), LYNC('lync')

	private static final def notifications = [:]

	static {
        NotificationProtocol[] notifsTab = NotificationProtocol.values()
		for (int i=0; i<notifsTab.size(); i++) {
			notifications[notifsTab[i].name] = notifsTab[i]
		}
	}

	static NotificationProtocol getFromProtocol(String protocol) {
		return notifications[protocol]
	}

    // Cela semble inutile, mais pas du tout
    // Quand on ne connait pas son type d'entrée, cela permet d'être sur d'avoir son Enum.
    static NotificationProtocol getFromProtocol(NotificationProtocol protocol) {
        return protocol
    }

    NotificationProtocol(String notificationProtocol) {
		this.name = notificationProtocol
	}

	private final String name

	@NonCPS
	public String toString() {
		return this.name
	}
}