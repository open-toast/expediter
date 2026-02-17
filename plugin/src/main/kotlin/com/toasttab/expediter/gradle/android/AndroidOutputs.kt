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

package com.toasttab.expediter.gradle.android

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import com.toasttab.expediter.gradle.ArtifactSelector
import com.toasttab.expediter.gradle.ExpediterTask
import com.toasttab.expediter.gradle.TaskWithProjectOutputs
import com.toasttab.expediter.gradle.config.AndroidApplicationSpec
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

fun Project.configureAndroidOutputs(taskProvider: TaskProvider<ExpediterTask>, artifactSelector: ArtifactSelector, specProvider: () -> AndroidApplicationSpec?) {
    project.whenAndroidPluginApplied {
        project.extensions.getByType(AndroidComponentsExtension::class.java).onVariants { variant ->
            val spec = specProvider()

            if (spec != null) {
                if (spec.variants.contains(variant.name)) {
                    variant.artifacts.forScope(ScopedArtifacts.Scope.PROJECT).use(taskProvider)
                        .toGet(
                            ScopedArtifact.CLASSES,
                            TaskWithProjectOutputs::projectOutputFiles,
                            TaskWithProjectOutputs::projectOutputDirs
                        )

                    if (spec.withRuntimeConfiguration) {
                        taskProvider.configure {
                            artifactCollection(artifactSelector.artifacts(variant.runtimeConfiguration))
                        }
                    }
                }
            }
        }
    }
}
