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

package com.toasttab.expediter.gradle

import org.gradle.api.Action

abstract class ExpediterExtension {
    var application: ApplicationClassSelector = ApplicationClassSelector(configuration = "runtimeClasspath", sourceSet = "main")
    var platform: PlatformClassSelector = PlatformClassSelector()

    val ignoreSpec = IgnoreSpec()

    @Deprecated("use the ignore closure instead", replaceWith = ReplaceWith("ignore { file = ignoreFile }"))
    var ignoreFile: Any? = null

    var failOnIssues: Boolean = false

    fun application(configure: Action<ApplicationClassSelector>) {
        application = ApplicationClassSelector()
        configure.execute(application)
    }

    fun platform(configure: Action<PlatformClassSelector>) {
        configure.execute(platform)
    }

    fun ignore(configure: Action<IgnoreSpec>) {
        configure.execute(ignoreSpec)
    }
}
