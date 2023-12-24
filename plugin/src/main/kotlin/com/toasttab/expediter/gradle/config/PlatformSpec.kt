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
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.newInstance
import org.slf4j.LoggerFactory
import javax.inject.Inject

open class PlatformSpec @Inject constructor(
    objectFactory: ObjectFactory
) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(PlatformSpec::class.java)
    }

    val animalSnifferConfigurations: MutableList<String> = mutableListOf()
    val expediterConfigurations: MutableList<String> = mutableListOf()
    val configurations: MutableList<String> = mutableListOf()

    var jvmVersion: Int? = null

    @Deprecated("use android closure instead")
    var androidSdk: Int? = null
        set(value) {
            LOGGER.warn("androidSdk property is deprecated and will be removed, use android { sdk = ... }")
            androidSpec.sdk = value
        }

    val androidSpec: AndroidSpec = objectFactory.newInstance()

    fun android(configure: Action<AndroidSpec>) {
        configure.execute(androidSpec)
    }

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
