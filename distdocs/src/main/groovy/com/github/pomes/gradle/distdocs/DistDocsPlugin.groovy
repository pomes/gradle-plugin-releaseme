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

package com.github.pomes.gradle.distdocs

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.GroovyPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.bundling.Jar

import static org.gradle.api.plugins.JavaPlugin.CLASSES_TASK_NAME
import static org.gradle.api.plugins.JavaPlugin.TEST_CLASSES_TASK_NAME
import static org.gradle.api.plugins.GroovyPlugin.GROOVYDOC_TASK_NAME

class DistDocsPlugin implements Plugin<Project> {
    static final String EXTENSION_NAME = 'distdocs'
    static final String TASK_GROUP_DOCUMENTATION = 'documentation'
    static final String TASK_GROUP_SOURCES = 'sources'

    @Override
    void apply(Project project) {
        if (project.plugins.hasPlugin(JavaPlugin)) {
            project.tasks.create(name: 'sourcesJar', type: Jar) {
                group TASK_GROUP_SOURCES
                dependsOn CLASSES_TASK_NAME
                inputs.sourceDir project.sourceSets.main.allSource
                classifier 'sources'
                from project.sourceSets.main.allSource
            }

            project.tasks.create(name: 'testSourcesJar', type: Jar) {
                group TASK_GROUP_SOURCES
                dependsOn TEST_CLASSES_TASK_NAME
                classifier 'test-sources'
                inputs.sourceDir project.sourceSets.test.allSource
                from project.sourceSets.test.allSource
            }
        }

        if (project.plugins.hasPlugin(GroovyPlugin)) {
            project.tasks.create(name: 'groovydocJar', type: Jar) {
                group TASK_GROUP_DOCUMENTATION
                dependsOn GROOVYDOC_TASK_NAME
                classifier 'groovydoc'
                inputs.sourceDir project.groovydoc.destinationDir
                from project.groovydoc.destinationDir
            }
        }
    }
}
