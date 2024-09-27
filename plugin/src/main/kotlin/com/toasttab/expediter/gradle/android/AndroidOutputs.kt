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
