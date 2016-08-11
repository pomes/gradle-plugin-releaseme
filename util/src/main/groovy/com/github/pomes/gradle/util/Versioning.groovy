package com.github.pomes.gradle.util


class Versioning {
    static final String DEFAULT_RELEASE_TAG_PREFIX = 'version-'

    static String determineNextReleaseVersion(String currentVersion,
                                              String releaseTagPrefix = DEFAULT_RELEASE_TAG_PREFIX) {
        "$currentVersion".endsWith("$Snapshot.SNAPSHOT") ? "$currentVersion" - "-$Snapshot.SNAPSHOT"
                : "$releaseTagPrefix-${currentVersion.tokenize('-')[1] + 1}"
    }

    static String determineMavenVersion(String version) {
        List<String> components = version.tokenize('-')
        String versionNumber = components.head()
        String postfix = components.size() > 1 ? "-${components.last()}" : ''
        "$versionNumber.0.0$postfix".toString()
    }
}
