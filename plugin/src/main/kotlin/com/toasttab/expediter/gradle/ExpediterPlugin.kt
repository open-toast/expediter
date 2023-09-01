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
                applicationClasspath.from(selector.artifacts(conf))
            }

            for (file in extension.application.files) {
                applicationClasspath.from(file)
            }

            for (sourceSet in extension.application.sourceSets) {
                applicationClasspath.from(project.sourceSet(sourceSet).java.classesDirectory)
            }

            platformClassloader = extension.platform.platformClassloader

            val animalSnifferConfigurations = extension.platform.animalSnifferConfigurations.toMutableList()

            if (extension.platform.androidSdk != null) {
                val config = project.configurations.create("_expediter_animal_sniffer_")
                project.dependencies.add(
                    config.name,
                    "com.toasttab.android:gummy-bears-api-${extension.platform.androidSdk}:0.5.1@signature"
                )
                animalSnifferConfigurations.add(config.name)
            }

            for (conf in animalSnifferConfigurations) {
                animalSnifferSignatures.from(project.configurations.getByName(conf))
            }

            ignore = extension.ignore
            ignoreFile = extension.ignoreFile?.let { project.file(it) }

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
