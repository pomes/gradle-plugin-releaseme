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
package com.github.pomes.gradle.releaseme

import com.github.pomes.gradle.projectinfo.ProjectInfoPlugin
import com.jfrog.bintray.gradle.BintrayPlugin
import groovy.util.logging.Slf4j
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.jfrog.gradle.plugin.artifactory.ArtifactoryPlugin

@Slf4j
class IShallBeReleasedPlugin implements Plugin<Project> {
    static final String EXTENSION_NAME = 'releaseme'

    //Tasks:
    static final String PERFORM_GITHUB_RELEASE = 'performGitHubRelease'

    IShallBeReleasedExtension extension

    @Override
    void apply(Project project) {
        extension = project.extensions.create(EXTENSION_NAME, IShallBeReleasedExtension)

        [ProjectInfoPlugin].each { plugin ->
            project.plugins.apply(plugin)
        }

        if (extension.releaseProject) {
            project.plugins.apply(MavenPublishPlugin)

            if (extension.artifactoryRelease) {
                project.plugins.apply(ArtifactoryPlugin)
            }

            if (extension.bintrayRelease) {
                project.plugins.apply(BintrayPlugin)
            }

            if (extension.githubRelease) {
                addPerformGitHubReleaseTask(project, extension)
            }
        }
    }

    private void addPerformGitHubReleaseTask(Project project, IShallBeReleasedExtension extension) {
        project.tasks.create(PERFORM_GITHUB_RELEASE) {
            group = 'release'
            description = 'Releases the application distribution to GitHub'
            doLast {
                println "Some day I'll release $project.name to GitHub"
            }
        }
    }
}
