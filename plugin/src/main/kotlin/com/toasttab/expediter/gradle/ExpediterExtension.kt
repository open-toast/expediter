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

import com.toasttab.expediter.ignore.Ignore

abstract class ExpediterExtension {
    var application: ApplicationClassSelector = ApplicationClassSelector(configuration = "runtimeClasspath")
    var platform: PlatformClassSelector = PlatformClassSelector(platformClassloader = true)

    var ignore: Ignore = Ignore.NOTHING
    var ignoreFile: Any? = null

    var failOnIssues: Boolean = false
    fun application(configure: ApplicationClassSelector.() -> Unit) {
        application = ApplicationClassSelector()
        application.configure()
    }
    fun platform(configure: PlatformClassSelector.() -> Unit) {
        platform = PlatformClassSelector(false)
        platform.configure()
    }
}
