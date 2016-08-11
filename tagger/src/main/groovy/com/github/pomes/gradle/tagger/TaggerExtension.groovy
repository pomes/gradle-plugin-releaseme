package com.github.pomes.gradle.tagger

import groovy.transform.ToString
import groovy.util.logging.Slf4j

@Slf4j
@ToString(includeNames = true)
class TaggerExtension {
    String releaseTagPrefix = DEFAULT_RELEASE_TAG_PREFIX
}
