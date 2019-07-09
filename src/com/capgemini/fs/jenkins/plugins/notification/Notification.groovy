#!/usr/bin/groovy
package com.capgemini.fs.jenkins.plugins.notification

interface Notification extends Serializable {

    Notification by(List protocols)

	Notification on(List levels)

	Notification on(NotificationLevel level)

    Notification withColor(String thisColor)

	Notification withIcon(String thisIcon)

    Notification withSlackOpts(String endpoint, String channel)

    boolean isSlackOptsOverrided()

	def getData(NotificationDataKeys data)

	boolean exists(NotificationDataKeys data)

	void appendData(String key, def value)

	void appendDatas(Map thisDatas)

	/**
	 * Definition des templates - utile uniquement pour les libraryResources
	 * @param script le dsl appelant
	 * @return Notification
	 */
    void setupTemplates(script)
}
