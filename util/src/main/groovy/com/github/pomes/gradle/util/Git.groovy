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

import groovy.util.logging.Slf4j
import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.Tag
import org.eclipse.jgit.errors.RepositoryNotFoundException
import org.gradle.api.GradleException

@Slf4j
class Git {
    static Grgit connectToLocalGit(String dir) {
        try {
            return Grgit.open(currentDir: dir)
        } catch (RepositoryNotFoundException e) {
            throw new GradleException("Git repository not found at $dir")
        }
    }

    static Tag determineLastReleaseVersion(Grgit git, String releaseTagPrefix) {
        List<Tag> tags = git.tag.list()

        if (tags) {
            tags.findAll { it.name.startsWith(releaseTagPrefix) }
                    .max { it.name - releaseTagPrefix }
        } else {
            null
        }
    }

    static String determineCurrentVersion(Grgit git, String releaseTagPrefix) {
        String currentVersion = '1'
        Boolean snapshot = true
        Tag latestVersionTag = determineLastReleaseVersion(git, releaseTagPrefix)

        if (latestVersionTag) {
            log.info "Latest version tag is $latestVersionTag.name"
            if (latestVersionTag.commit.id == git.head().id && git.status().clean) {
                //Code is currently on a version tag
                currentVersion = latestVersionTag.name - releaseTagPrefix
                snapshot = false
            } else {
                currentVersion = (latestVersionTag.name - releaseTagPrefix).toInteger() + 1
            }
        }
        log.info "Current version is $currentVersion"
        log.info "Version is a snapshot: $snapshot"
        snapshot ? "$currentVersion-${Snapshot.SNAPSHOT}" : "$currentVersion"
    }
}
