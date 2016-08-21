/*
 *    Copyright 2016 Duncan Dickinson
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.github.pomes.gradle.util

import org.gradle.api.Project

class Versioning {
    static final String DEFAULT_RELEASE_TAG_PREFIX = 'version-'
    static final String VERSION_FILE_NAME = 'VERSION'

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

    static String setVersionForProject(Project project, String version = '1', File versionFile = project.file(VERSION_FILE_NAME)) {
        project.ext.lastVersion = project.version
        project.version = version
        versionFile.text = project.version
        project.subprojects.each { sub ->
            sub.version = project.version
        }
        return version
    }

    static String formatTagName(String version) {
        "$DEFAULT_RELEASE_TAG_PREFIX$version"
    }
}
