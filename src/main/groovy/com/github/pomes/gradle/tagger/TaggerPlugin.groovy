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
import org.gradle.api.plugins.JavaBasePlugin
import org.kohsuke.github.GHRepository

@Slf4j
class TaggerPlugin implements Plugin<Project> {
    static final String PLUGIN_NAME = 'tagger'
    static final String PLUGIN_GROUP = PLUGIN_NAME
    static final String CHECK_RELEASE_STATUS_TASK_NAME = 'checkTagReadiness'
    static final String PERFORM_RELEASE_TASK_NAME = 'tagRepository'

    Grgit localGit
    GHRepository ghRepo

    @Override
    void apply(Project project) {
        GitInfo info = GitBasePlugin.applyPlugin(project)
        localGit = info.localGit
        ghRepo = info.githubRepo
        GitBaseExtension extension = project.extensions.getByName(GitBasePlugin.EXTENSION_NAME)

        Task checkRelease = project.tasks.create(CHECK_RELEASE_STATUS_TASK_NAME) {
            group = PLUGIN_GROUP
            description = 'Checks if there are any items preventing tagging the repository.'

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
                if (p.plugins.hasPlugin(JavaBasePlugin)) {
                    checkRelease.dependsOn p.tasks.getByName(JavaBasePlugin.BUILD_TASK_NAME)
                }
            }
        }

        project.tasks.create(PERFORM_RELEASE_TASK_NAME) {
            group = PLUGIN_GROUP
            description = 'Tags the git repository.'
            String tag
            doFirst {
                tag = "${extension.releaseTagPrefix}${project.version}"
                localGit.commit(message: "Version ${project.version} release", all: true)
            }
            doLast {
                println "Tagging with: $tag"
                localGit.tag.add(name: tag)
            }
        }
    }
}
