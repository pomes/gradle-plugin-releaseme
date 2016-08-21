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

package com.github.pomes.gradle.projectinfo.project

import groovy.transform.Canonical

import java.time.Year

@Canonical
class ProjectInfo {
    String name, description, version
    URL url
    Year inceptionYear
    Scm scm
    List<License> licenses
    IssueManagement issueManagement
    CiManagement ciManagement

    void toYaml(Writer writer) {
        writer << "project: \n"
        writer << "  name: $name\n"
        writer << "  version: $version\n"
        writer << "  description: $description\n"
        writer << "  url: $url\n"
        writer << "  inceptionYear: $inceptionYear\n"
        writer << "  scm:\n"
        scm?.toYaml(writer)
        writer << "  licenses: \n"
        licenses?.each {lic ->
            writer << "    - \n"
            lic.toYaml(writer, ' '*5)
        }
        writer << "  issueManagement: \n"
        issueManagement?.toYaml(writer)
        writer << "  ciManagement: \n"
        ciManagement?.toYaml(writer)
    }

    String toYaml() {
        StringWriter writer = new StringWriter()
        toYaml(writer)
        writer.toString()
    }
}

@Canonical
class Scm {
    String system
    URL url
    String connection, developerConnection

    void toYaml(Writer writer = new StringWriter(), prefix = ' ' * 4) {
        writer << "${prefix}system: $system\n"
        writer << "${prefix}url: $url\n"
        writer << "${prefix}connection: $connection\n"
        writer << "${prefix}developerConnection: $developerConnection\n"
    }
}

@Canonical
class License {
    String name
    URL url

    void toYaml(Writer writer = new StringWriter(), prefix = ' ' * 4) {
        writer << "${prefix}name: $name\n"
        writer << "${prefix}url: $url\n"
    }
}

@Canonical
class IssueManagement {
    String system
    URL url

    void toYaml(Writer writer = new StringWriter(), prefix = ' '*4) {
        writer << "${prefix}system: $system\n"
        writer << "${prefix}url: $url\n"
    }
}

@Canonical
class CiManagement {
    String system
    URL url

    void toYaml(Writer writer = new StringWriter(), prefix = ' '*4) {
        writer << "${prefix}system: $system\n"
        writer << "${prefix}url: $url\n"
    }
}
