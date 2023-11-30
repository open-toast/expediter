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

class ApplicationSpec(
    val configurations: MutableList<String>,
    val files: MutableList<String> = mutableListOf(),
    val sourceSets: MutableList<String> = mutableListOf()
) {
    constructor() : this(mutableListOf())

    constructor(configuration: String, sourceSet: String) : this(configurations = mutableListOf(configuration), sourceSets = mutableListOf(sourceSet))

    fun configuration(configuration: String) {
        configurations.add(configuration)
    }

    fun sourceSet(sourceSet: String) {
        sourceSets.add(sourceSet)
    }
}
