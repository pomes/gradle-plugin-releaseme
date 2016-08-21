package com.github.pomes.gradle.tagger

import com.github.pomes.gradle.gitbase.GitBaseExtension
import com.github.pomes.gradle.gitbase.GitBasePlugin
import com.github.pomes.gradle.gitbase.GitInfo
import groovy.util.logging.Slf4j
import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.Status
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.kohsuke.github.GHRepository

@Slf4j
class TaggerPlugin implements Plugin<Project> {
    static final String PLUGIN_NAME = 'tagger'
    static final String CHECK_RELEASE_STATUS_TASK_NAME = 'checkReleaseStatus'
    static final String PERFORM_RELEASE_TASK_NAME = 'performRelease'

    Grgit localGit
    GHRepository ghRepo

    @Override
    void apply(Project project) {
        GitInfo info = GitBasePlugin.applyPlugin(project)
        localGit = info.localGit
        ghRepo = info.githubRepo
        GitBaseExtension extension = project.extensions.getByName(GitBasePlugin.EXTENSION_NAME)

        addCheckReleaseStatusTask(project)
        addPerformReleaseTask(project, extension)
    }

    private void addCheckReleaseStatusTask(Project project) {
        Task checkRelease = project.tasks.create(CHECK_RELEASE_STATUS_TASK_NAME) {
            group = 'release'
            description = 'Checks if there are any items preventing a release.'

            doLast {
                Status status = localGit.status()
                Boolean flag = false
                List<String> errors = []

                if (!status.clean) {
                    log.warn "The local git repository contains changes: Conflicts: ${status.conflicts.size()}; Staged: ${status.staged.allChanges.size()}; Unstaged: ${status.unstaged.allChanges.size()}"
                    flag = true
                }

                if (localGit.branch.current.name != ghRepo.defaultBranch) {
                    errors << "You don't currently appear to be on the default branch (${ghRepo.defaultBranch}) - time to merge (${localGit.branch.current.fullName})."
                    flag = true
                }

                if (!project.file('LICENSE').exists()) {
                    errors << 'You don\'t have a LICENSE file'
                    flag = true
                }

                if (!flag) {
                    log.info 'No issues detected - time to release!'
                } else {
                    errors.each {
                        log.error it
                    }
                    throw new GradleException('Issues detected - cannot perform a release!')
                }
            }
        }

        project.afterEvaluate {
            project.allprojects.each { p ->
                if (p.plugins.hasPlugin('java')) {
                    checkRelease.dependsOn p.tasks.getByName('build')
                }
            }
        }
    }

    private void addPerformReleaseTask(Project project, GitBaseExtension extension) {
        project.tasks.create('_prepareReleaseVersion') {
            group = 'release'
            description = 'Prepares any changes required prior to committing/tagging a release'
            dependsOn CHECK_RELEASE_STATUS_TASK_NAME
            doLast {
                //Change the version to drop the SNAPSHOT
                //String releaseVersion = determineNextReleaseVersion(project.version, extension.releaseTagPrefix)
                //Versioning.setVersionForProject(project, releaseVersion)
                println "The project version will be '$project.version' for the release"
            }
        }

        project.tasks.create('_commitRelease') {
            group = 'release'
            description = 'Tags a release in git.'
            dependsOn '_prepareReleaseVersion'
            doLast {
                localGit.commit(message: "Version ${project.version} release", all: true)
            }
        }

        project.tasks.create('_tagRelease') {
            group = 'release'
            description = 'Tags a release in git.'
            dependsOn '_commitRelease'
            doLast {
                String tag = "${extension.releaseTagPrefix}${project.version}"
                println "Releasing $tag"
                localGit.tag.add(name: tag)
            }
        }

        project.tasks.create(PERFORM_RELEASE_TASK_NAME) {
            group = 'release'
            description = 'Performs the release.'
            dependsOn '_tagRelease'
            doLast {
            }
        }
    }
}
