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

import com.github.pomes.gradle.gitbase.GitBaseExtension
import com.github.pomes.gradle.gitbase.GitBasePlugin
import com.github.pomes.gradle.gitbase.GitInfo
import com.github.pomes.gradle.projectinfo.project.*
import com.github.pomes.gradle.util.GitUtil
import com.github.pomes.gradle.util.Versioning
import groovy.util.logging.Slf4j
import org.ajoberstar.grgit.Grgit
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.kohsuke.github.GHRepository

import java.time.Year
import java.time.ZoneId

@Slf4j
class ProjectInfoPlugin implements Plugin<Project> {

    static final String PLUGIN_NAME = 'tagger'

    static final String TASK_GROUP = 'project info'

    //Tasks
    static final String DETERMINE_VERSION_TASK_NAME = 'determineCurrentVersion'
    static final String DISPLAY_VERSION_TASK_NAME = 'displayCurrentVersion'
    //static final String CONFIGURE_VERSION_FILE_TASK_NAME = 'configureVersionFile'
    static final String GENERATE_PROJECT_INFO_TASK_NAME = 'generateProjectInfo'
    static final String DISPLAY_PROJECT_INFO_TASK_NAME = 'displayProjectInfo'
    static final String CONFIGURE_POM_TASK_NAME = 'configurePom'

    Grgit localGit
    GHRepository ghRepo
    GitBaseExtension extension

    @Override
    void apply(Project project) {
        GitInfo info = GitBasePlugin.applyPlugin(project)
        localGit = info.localGit
        ghRepo = info.githubRepo
        extension = project.extensions.getByName(GitBasePlugin.EXTENSION_NAME)

        String currentVersion = GitUtil.determineCurrentVersion(localGit,
                extension.releaseTagPrefix)
        Versioning.setVersionForProject(project, currentVersion)
        log.info "Project ($project.name) version set to $project.version"

        project.allprojects.each { p ->
            p.ext.projectInfo = generateProjectInfo(p, ghRepo)
            addDisplayVersionTask(p)
            addDisplayProjectInfoTask(p)
            addConfigurePomTask(p)
        }
    }


    private void addDisplayVersionTask(Project project) {
        project.tasks.create(DISPLAY_VERSION_TASK_NAME) {
            group = 'release'
            description = 'Displays the current version.'
            doLast {
                println project.version
            }
        }
    }

    private void addDisplayProjectInfoTask(Project project) {
        project.tasks.create(DISPLAY_PROJECT_INFO_TASK_NAME) {
            group = TASK_GROUP
            description = 'Displays project details.'
            doLast {
                println project.ext.projectInfo.toYaml()
            }
        }
    }

    static ProjectInfo generateProjectInfo(Project project, GHRepository ghRepo) {
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

    }

    private void addConfigurePomTask(Project project) {
        project.tasks.create(CONFIGURE_POM_TASK_NAME) {
            group = TASK_GROUP
            description = 'Configures project\'s POM'
            doLast {
                project.ext.pom = generatePomNodes(project.ext.projectInfo)
            }
        }
    }

    Node generatePomNodes(ProjectInfo info) {
        new NodeBuilder().pom {
            name info.name
            description info.description ?: 'No description'
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
}
