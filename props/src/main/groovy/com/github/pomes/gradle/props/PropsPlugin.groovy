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

package com.github.pomes.gradle.props

import groovy.util.logging.Slf4j
import org.gradle.api.Plugin
import org.gradle.api.Project

@Slf4j
class PropsPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def propertyFiles = project.container(PropertyFile)

        propertyFiles.all {
            sourceFile = project.file("${name}.properties")
        }

        propertyFiles.each { PropertyFile prop ->
            if (!prop.sourceFile.exists()) {
                log.debug "Could not find the requested properties file: ${prop.sourceFile}"
            } else {
                prop.sourceFile.withInputStream { stream ->
                    prop.properties.load(stream)
                }
            }
        }
        project.extensions.propertyFiles = propertyFiles
    }
}

class PropertyFile {
    final String name
    final Properties properties = new Properties()

    File sourceFile

    PropertyFile(String name) {
        this.name = name
    }
}
