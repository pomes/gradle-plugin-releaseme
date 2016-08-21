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
import com.github.pomes.gradle.util.Versioning
import com.jfrog.bintray.gradle.BintrayPlugin
import groovy.util.logging.Slf4j
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.jfrog.gradle.plugin.artifactory.ArtifactoryPlugin

@Slf4j
class IShallBeReleasedPlugin implements Plugin<Project> {
    static final String PLUGIN_NAME = 'releaseme'
    static final String EXTENSION_NAME = PLUGIN_NAME
    static final String PUBLICATION_NAME = PLUGIN_NAME

    //Tasks:
    static final String PERFORM_GITHUB_RELEASE = 'performGitHubRelease'

    IShallBeReleasedExtension extension

    @Override
    void apply(Project project) {
        extension = project.extensions.create(EXTENSION_NAME, IShallBeReleasedExtension)
        log.info "Releaseme config: extension"
        [ProjectInfoPlugin].each { plugin ->
            project.plugins.apply(plugin)
        }

        if (extension.releaseProject) {
            project.plugins.apply(MavenBasePublishPlugin)
            project.publishing {
                publications {
                    releaseme(MavenPublication) {
                        from components.java
                        artifact tasks.groovydocJar
                        artifact tasks.sourcesJar
                        version Versioning.determineMavenVersion(project.version)
                        pom.withXml {
                            project.ext.pom.each { n ->
                                asNode().append n
                            }
                        }
                    }
                }
            }

            if (extension.artifactoryRelease) {
                project.plugins.apply(ArtifactoryPlugin)
                project.artifactory {
                    contextUrl = extension.artifactoryContextUrl
                    publish {
                        repository {
                            repoKey = extension.artifactoryRepoKey
                            username = extension.bintrayUser
                            password = extension.bintrayPassword
                        }
                        publications(PUBLICATION_NAME)
                    }
                }
            }

            if (extension.bintrayRelease) {
                project.plugins.apply(BintrayPlugin)
                project.bintray {
                    user = extension.bintrayUser
                    key = extension.bintrayPassword
                    publications = [PUBLICATION_NAME]
                    pkg {
                        userOrg = extension.bintrayUserOrg
                        repo = extension.bintrayRepo
                        name = project.name
                        labels = extension.projectKeywords
                        version {
                            name = project.version
                            vcsTag = Versioning.formatTagName(project.version)
                        }
                    }
                }
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
