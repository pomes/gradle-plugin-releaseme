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

package com.github.pomes.gradle.projectinfo

import com.github.pomes.gradle.projectinfo.project.*
import com.github.pomes.gradle.releaseme.IShallBeReleasedPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

import java.time.Year
import java.time.ZoneId

class ProjectInfoPlugin implements Plugin<Project> {
    static final String EXTENSION_NAME = 'projectinfo'

    static final String TASK_GROUP = 'project info'

    static final String GENERATE_PROJECT_INFO_TASK_NAME = 'generateProjectInfo'
    static final String DISPLAY_PROJECT_INFO_TASK_NAME = 'displayProjectInfo'
    static final String CONFIGURE_POM_TASK_NAME = 'configurePom'

    private ProjectInfo projectInfo
    private Node pom

    @Override
    void apply(Project project) {
        project.extensions.create(EXTENSION_NAME, ProjectInfoExtension)

        addGenerateProjectInfoTask(project)
        addDisplayProjectInfoTask(project)
        addConfigurePomTask(project)
    }

    private void addGenerateProjectInfoTask(Project project) {
        project.tasks.create(GENERATE_PROJECT_INFO_TASK_NAME) {
            group = TASK_GROUP
            description = 'Gathers together various project details.'
            if (project.rootProject.plugins.hasPlugin(IShallBeReleasedPlugin)) {
                dependsOn project.rootProject.tasks.getByName(IShallBeReleasedPlugin.DETERMINE_VERSION_TASK_NAME)
            }
            doLast {
                if (!projectInfo) {
                    projectInfo = generateProjectInfo(project)
                    project.ext.projectInfo = projectInfo
                }
            }
        }
    }

    private void addDisplayProjectInfoTask(Project project) {
        project.tasks.create(DISPLAY_PROJECT_INFO_TASK_NAME) {
            group = TASK_GROUP
            description = 'Displays project details.'
            dependsOn GENERATE_PROJECT_INFO_TASK_NAME
            doLast {
                println project.ext.projectInfo.toYaml()
            }
        }
    }

    static ProjectInfo generateProjectInfo(Project project) {
        if (project.rootProject.ext.has('ghRepo')) {
            def ghRepo = project.rootProject.ext.ghRepo
            return new ProjectInfo(
                    name: project.name,
                    version: project.version,
                    description: project.description ?: ghRepo.description,
                    url: ghRepo.homepage.toURL(),
                    inceptionYear: new Year(ghRepo.createdAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().year),
                    scm: [system             : 'git',
                          url                : ghRepo.gitHttpTransportUrl().toURL(),
                          connection         : "scm:git:${ghRepo.gitHttpTransportUrl()}",
                          developerConnection: "scm:git:${ghRepo.gitHttpTransportUrl()}"] as Scm,
                    licenses: [[name: ghRepo.license.name, url: ghRepo.license.url] as License],
                    issueManagement: [system: 'GitHub', url: "${ghRepo.htmlUrl}/issues".toURL()] as IssueManagement,
                    ciManagement: [system: 'TravisCI', url: "https://travis-ci.org/${ghRepo.fullName}".toURL()] as CiManagement
            )
        } else {
            return new ProjectInfo(
                    name: project.name,
                    version: project.version,
                    description: project.description)
        }
    }

    private void addConfigurePomTask(Project project) {
        project.tasks.create(CONFIGURE_POM_TASK_NAME) {
            group = TASK_GROUP
            description = 'Configures project\'s POM'
            dependsOn GENERATE_PROJECT_INFO_TASK_NAME
            doLast {
                project.ext.pom = generatePomNodes(projectInfo)
            }
        }
    }

    Node generatePomNodes(ProjectInfo info) {
        new NodeBuilder().pom {
            name info.name
            description info.description?:'No description'
            if (info.url) url info.url
            if (info.inceptionYear) inceptionYear info.inceptionYear
            if (info.scm) {
                scm {
                    url info.scm.url
                    connection info.scm.connection
                    developerConnection info.scm.developerConnection
                }
            }
            if (info.licenses) {
                licenses {
                    info.licenses.each { License lic ->
                        license {
                            name lic.name
                            url lic.url
                        }
                    }
                }
            }
            if (info.issueManagement) {
                issueManagement {
                    system info.issueManagement.system
                    url info.issueManagement.url
                }
            }
            if (info.ciManagement) {
                ciManagement {
                    system info.ciManagement.system
                    url info.ciManagement.url
                }
            }
        }
    }

    static String determineMavenVersion(String version) {
        List<String> components = version.tokenize('-')
        String versionNumber = components.head()
        String postfix = components.size() > 1 ? "-${components.last()}" : ''
        "$versionNumber.0.0$postfix".toString()
    }
}
