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

import org.gradle.api.Project
import org.gradle.api.artifacts.ArtifactCollection
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.attributes.Attribute
import org.gradle.api.internal.tasks.DefaultTaskDependency
import org.gradle.kotlin.dsl.artifacts
import java.io.File

private val ARTIFACT_TYPE_ATTR = Attribute.of("artifactType", String::class.java)

class ArtifactSelector(
    private val project: Project,
    private var artifactType: String = "jar"
) {
    private fun android() {
        artifactType = "android-classes"
    }

    init {
        project.pluginManager.withPlugin("com.android.library") {
            android()
        }

        project.pluginManager.withPlugin("com.android.application") {
            android()
        }
    }

    fun artifacts(configuration: String): ArtifactCollection {
        return project.configurations.getByName(configuration).incoming.artifactView {
            lenient(true)
            attributes.attribute(ARTIFACT_TYPE_ATTR, artifactType)
        }.artifacts
    }
}
