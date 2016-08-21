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

package com.github.pomes.gradle.releaseme

import groovy.transform.ToString
import groovy.util.logging.Slf4j

@Slf4j
@ToString(includeNames = true, excludes = ['bintrayPassword'])
class IShallBeReleasedExtension {
    Boolean releaseProject = false

    Boolean githubRelease = false

    Boolean bintrayRelease = false

    Boolean artifactoryRelease = false

    String bintrayUser = ''

    String bintrayPassword = ''

    String bintrayUserOrg = ''

    String bintrayRepo = ''

    String projectKeywords = []

    String artifactoryRepoKey = 'oss-snapshot-local'

    String artifactoryContextUrl = 'https://oss.jfrog.org/artifactory/'
}
