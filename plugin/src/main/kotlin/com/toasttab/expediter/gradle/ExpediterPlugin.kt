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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

class ExpediterPlugin : Plugin<Project> {
    private fun Project.sourceSet(sourceSet: String) = extensions.getByType<SourceSetContainer>().getByName(sourceSet)

    override fun apply(project: Project) {
        val extension = project.extensions.create<ExpediterExtension>("expediter")
        val selector = ArtifactSelector(project)

        project.tasks.register<ExpediterTask>("expedite") {
            for (conf in extension.application.configurations) {
                artifactCollection(selector.artifacts(conf))
            }

            for (file in extension.application.files) {
                files.from(file)
            }

            for (sourceSet in extension.application.sourceSets) {
                files.from(project.sourceSet(sourceSet).java.classesDirectory)
            }

            jvmVersion = extension.platform.jvmVersion

            val expediterConfigurations = extension.platform.expediterConfigurations.toMutableList()

            if (extension.platform.androidSdk != null) {
                val config = project.configurations.create("_expediter_type_descriptors_")
                project.dependencies.add(
                    config.name,
                    "com.toasttab.android:gummy-bears-api-${extension.platform.androidSdk}:0.6.0@expediter"
                )
                expediterConfigurations.add(config.name)
            }

            for (conf in expediterConfigurations) {
                typeDescriptors.from(project.configurations.getByName(conf))
            }

            for (conf in extension.platform.animalSnifferConfigurations) {
                animalSnifferSignatures.from(project.configurations.getByName(conf))
            }

            if (extension.platform.jvmVersion != null && extension.platform.androidSdk != null) {
                logger.warn("Both jvmVersion and androidSdk are set.")
            }

            for (conf in extension.platform.configurations) {
                platformArtifactCollection(selector.artifacts(conf))
            }

            ignore = extension.ignoreSpec.buildIgnore()

            @Suppress("DEPRECATION")
            ignoreFile = (extension.ignoreSpec.file ?: extension.ignoreFile)?.let { project.file(it) }

            report = project.layout.buildDirectory.file("expediter.json").get().asFile

            failOnIssues = extension.failOnIssues
        }

        project.tasks.named("check") {
            if (extension.failOnIssues) {
                dependsOn("expedite")
            }
        }
    }
}
