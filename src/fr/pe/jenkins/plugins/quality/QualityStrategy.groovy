#!/usr/bin/groovy
package fr.pe.jenkins.plugins.quality

enum QualityStrategy {
	STANDARD(1),
	UNSTABLE_ANYWAY(2),
	ABORT_ANYWAY(3)

    QualityStrategy(int strategyLevel) {
		this.level = strategyLevel
	}
	private final int level

	public int getLevel() {
		return level
	}

	public String toString() {
		return "${level}"
	}
}