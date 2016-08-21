package com.github.pomes.gradle.gitbase

import com.github.pomes.gradle.util.GitHubUtil
import com.github.pomes.gradle.util.GitUtil
import com.github.pomes.gradle.util.Versioning
import org.ajoberstar.grgit.Grgit
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.kohsuke.github.GHRepository

class GitBasePlugin implements Plugin<Project> {

    static final String EXTENSION_NAME = 'gitbase'
    static final String PLUGIN_NAME = 'gitbase'

    GitBaseExtension extension

    @Override
    void apply(Project project) {
        extension = project.extensions.create(EXTENSION_NAME, GitBaseExtension)

        Grgit localGit = GitUtil.connectToLocalGit(project.rootDir.toString())
        if (!localGit) {
            throw new GradleException('Failed to connect to the local GitUtil repository.')
        }

        GHRepository ghRepository = GitHubUtil.connectToGithub(localGit, extension.remote)
        project.ext.gitInfo = new GitInfo(localGit: localGit,
                githubRepo: ghRepository)
    }

    static GitInfo applyPlugin(Project project) throws GradleException {
        if (!project.pluginManager.hasPlugin(PLUGIN_NAME)) {
            project.plugins.apply(GitBasePlugin)
        }

        if (!project.ext.gitInfo) {
            throw GradleException('Failed to access the local git repository using the gitbase plugin')
        }
        return project.ext.gitInfo
    }
}

class GitBaseExtension {
    String remote = 'origin'
    String releaseTagPrefix = Versioning.DEFAULT_RELEASE_TAG_PREFIX
}

class GitInfo {
    Grgit localGit
    GHRepository githubRepo
}
