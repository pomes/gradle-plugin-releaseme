package com.github.pomes.gradle.tagger

import groovy.util.logging.Slf4j
import org.ajoberstar.grgit.Status
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

import java.nio.file.Files
import java.nio.file.Paths

@Slf4j
class TaggerPlugin implements Plugin<Project> {
    static final String CHECK_RELEASE_STATUS_TASK_NAME = 'checkReleaseStatus'
    static final String PERFORM_RELEASE_TASK_NAME = 'performRelease'

    @Override
    void apply(Project project) {
        addCheckReleaseStatusTask(project, extension)
        addPerformReleaseTask(project, extension)
    }

    private void addCheckReleaseStatusTask(Project project, TaggerExtension extension) {
        project.tasks.create(CHECK_RELEASE_STATUS_TASK_NAME) {
            group = 'release'
            description = 'Checks if there are any items preventing a release.'
            doLast {
                Status status = project.ext.gitInfo.localGit.status()
                Boolean flag = false
                List<String> errors = []

                if (!status.clean) {
                    errors << "The local git repository contains changes: Conflicts: ${status.conflicts.size()}; Staged: ${status.staged.allChanges.size()}; Unstaged: ${status.unstaged.allChanges.size()}"
                    flag = true
                }

                if (localGit.branch.current.name != ghRepo.defaultBranch) {
                    errors << "You don't currently appear to be on the default branch (${ghRepo.defaultBranch}) - time to merge (${localGit.branch.current.fullName})."
                    flag = true
                }

                if (Files.notExists(Paths.get(project.rootDir.toString(), 'LICENSE'))) {
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
    }

    private void addPerformReleaseTask(Project project, TaggerExtension extension) {

        project.tasks.create('_prepareReleaseVersion') {
            group = 'release'
            description = 'Prepares any changes required prior to committing/tagging a release'
            dependsOn CHECK_RELEASE_STATUS_TASK_NAME
            finalizedBy CONFIGURE_VERSION_FILE_TASK_NAME
            doLast {
                //Change the version to drop the SNAPSHOT
                project.version = determineNextReleaseVersion(project.version)
                setVersionForProject(project)
                println "The project version is set to '$project.version' for the release"
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
                String tag = "${DEFAULT_RELEASE_TAG_PREFIX}${project.version}"
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
