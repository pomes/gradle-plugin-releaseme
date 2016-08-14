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
import com.github.pomes.gradle.tagger.TaggerPlugin
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

        [TaggerPlugin, ProjectInfoPlugin].each { plugin ->
            applyPlugin(project, plugin)
        }

        project.subprojects.each { Project subProject ->
            [MavenPublishPlugin, BintrayPlugin, ArtifactoryPlugin].each { plugin ->
                applyPlugin(subProject, plugin)
            }
        }

        //addPerformGitHubReleaseTask(project, extension)
    }

    static void applyPlugin(Project project, Class<Plugin> plugin) {
        if (!project.plugins.hasPlugin(plugin)) {
            project.plugins.apply(plugin)
        }
    }

    private void addPerformGitHubReleaseTask(Project project, IShallBeReleasedExtension extension) {
        project.tasks.create(PERFORM_GITHUB_RELEASE) {
            group = 'release'
            description = 'Releases the application distribution to GitHub'
            doLast {
                project.allprojects.each { proj ->
                    if (proj.pluginManager.hasPlugin('application')) {
                        println proj.name
                    }
                }

                //performGithubRelease(ghRepo)
            }
        }
    }


}
