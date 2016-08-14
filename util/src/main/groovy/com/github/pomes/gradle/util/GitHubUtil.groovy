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
import org.apache.commons.validator.routines.UrlValidator
import org.gradle.api.GradleException
import org.kohsuke.github.GHRelease
import org.kohsuke.github.GHReleaseBuilder
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub

@Slf4j
class GitHubUtil {

    static GHRepository connectToGithub(Grgit localGit, String remote = 'origin') throws GradleException {
        String ghProject

        String ghConnection = localGit.remote.list().find { it.name == remote }?.url
        log.info "Remote GitHub connection: $ghConnection"

        if (ghConnection.startsWith('git@github.com')) {
            ghProject = ghConnection.tokenize(':')[1] - '.git'
        } else {
            UrlValidator urlValidator = new UrlValidator()
            if (urlValidator.isValid(ghConnection)) {
                ghProject = (ghConnection.toURL().path - '.git').substring(1)
            } else {
                throw new GradleException("Unable to determine the Github project for $ghConnection")
            }
        }
        log.debug "GitHub project: $ghProject"

        try {
            return GitHub.connect().getRepository(ghProject)
        } catch (IOException ex) {
            throw new GradleException("Failed when trying to connect to GitHub project ($ghProject)")
        }
    }

    static GHRelease performGithubRelease(GHRepository ghRepo, String tag) {
        GHReleaseBuilder releasePrep = new GHReleaseBuilder(ghRepo, tag)
                .body("TODO: add release notes")
                .draft(true)

        GHRelease release = releasePrep.create()

        //TODO: Upload the archives created by the Application plugin
        // release.uploadAsset()
        release.draft = false
        return release
    }
}
