/*
 * Copyright (c) 2026 Toast Inc.
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

import com.toasttab.expediter.gradle.android.whenAndroidPluginApplied
import org.gradle.api.Project
import org.gradle.api.artifacts.ArtifactCollection
import org.gradle.api.artifacts.Configuration
import org.gradle.api.attributes.Attribute
import org.gradle.kotlin.dsl.artifacts

private val ARTIFACT_TYPE_ATTR = Attribute.of("artifactType", String::class.java)

class ArtifactSelector(
    private val project: Project,
    private var artifactType: String = "jar"
) {
    init {
        project.whenAndroidPluginApplied {
            artifactType = "android-classes"
        }
    }

    fun artifacts(configuration: String): ArtifactCollection {
        return artifacts(project.configurations.getByName(configuration))
    }

    fun artifacts(configuration: Configuration): ArtifactCollection {
        return configuration.incoming.artifactView {
            lenient(true)
            attributes.attribute(ARTIFACT_TYPE_ATTR, artifactType)
        }.artifacts
    }
}
