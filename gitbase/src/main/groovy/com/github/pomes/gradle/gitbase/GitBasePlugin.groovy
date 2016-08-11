package com.github.pomes.gradle.gitbase

import com.github.pomes.gradle.util.GitHubUtil
import com.github.pomes.gradle.util.GitUtil
import org.ajoberstar.grgit.Grgit
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.kohsuke.github.GHRepository

class GitBasePlugin implements Plugin<Project> {
    static final String EXTENSION_NAME = 'gitbase'

    GitBaseExtension extension

    @Override
    void apply(Project project) {
        extension = project.extensions.create(EXTENSION_NAME, GitBaseExtension)

        Grgit localGit = GitUtil.connectToLocalGit(project.rootDir.toString())
        if (!localGit) {
            throw new GradleException('Failed to connect to the local GitUtil repository.')
        }

        project.ext.gitInfo = new GitInfo(localGit: localGit,
                githubRepo: GitHubUtil.connectToGithub(localGit, extension.remote))
    }
}

class GitInfo {
    Grgit localGit
    GHRepository githubRepo
}
