/*
 * Copyright (c) 2023 Toast Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.toasttab.expediter.gradle.config

import org.gradle.api.Action
import org.slf4j.LoggerFactory

abstract class ExpediterExtension {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(ExpediterExtension::class.java)
    }

    var application: ApplicationSpec = ApplicationSpec(configuration = "runtimeClasspath", sourceSet = "main")
    var platform: PlatformSpec = PlatformSpec()

    val ignoreSpec = IgnoreSpec()

    var failOnIssues: Boolean = false

    @Deprecated("use ignore closure instead")
    var ignoreFile: Any? = null
        set(value) {
            LOGGER.warn("ignoreFile property is deprecated and will be removed, use ignore { file = ... }")
            ignoreSpec.file = value
        }

    fun application(configure: Action<ApplicationSpec>) {
        application = ApplicationSpec()
        configure.execute(application)
    }

    fun platform(configure: Action<PlatformSpec>) {
        configure.execute(platform)
    }

    fun ignore(configure: Action<IgnoreSpec>) {
        configure.execute(ignoreSpec)
    }
}
