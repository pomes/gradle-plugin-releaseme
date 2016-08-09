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
import org.kohsuke.github.GitHub

import static com.github.pomes.gradle.releaseme.IShallBeReleasedPlugin.DEFAULT_RELEASE_TAG_PREFIX

@Slf4j
@ToString(includeNames = true)
class IShallBeReleasedExtension {

    String remote = 'origin'

    String releaseTagPrefix = DEFAULT_RELEASE_TAG_PREFIX

    //Boolean releaseProject = false

    //Boolean githubRelease = false

    //Boolean bintrayRelease = false

    GitHub gitHub = null
}
