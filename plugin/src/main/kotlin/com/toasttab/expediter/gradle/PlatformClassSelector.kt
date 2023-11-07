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

class PlatformClassSelector(
    val animalSnifferConfigurations: MutableList<String> = mutableListOf(),
    val expediterConfigurations: MutableList<String> = mutableListOf(),
    val configurations: MutableList<String> = mutableListOf(),
    var androidSdk: Int? = null,
    var jvmVersion: Int? = null
) {
    fun animalSnifferConfiguration(configuration: String) {
        animalSnifferConfigurations.add(configuration)
    }

    fun expediterConfiguration(configuration: String) {
        expediterConfigurations.add(configuration)
    }

    fun configuration(configuration: String) {
        configurations.add(configuration)
    }
}
