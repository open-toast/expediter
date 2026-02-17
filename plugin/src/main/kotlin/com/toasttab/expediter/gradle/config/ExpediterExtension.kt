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

package com.toasttab.expediter.gradle.config

import com.toasttab.expediter.gradle.ArtifactSelector
import com.toasttab.expediter.gradle.ExpediterTask
import com.toasttab.expediter.gradle.android.configureAndroidOutputs
import com.toasttab.expediter.gradle.service.ApplicationTypeCache
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.register

abstract class ExpediterExtension(
    private val project: Project,
    private val sharedCache: Provider<ApplicationTypeCache>
) {
    private val selector = ArtifactSelector(project)

    private val specs = mutableMapOf<CheckKey, ExpediterCheckSpec>()

    private val defaultChecks by lazy {
        check("default")
    }

    var failOnIssues: Boolean = false
        set(value) {
            defaultChecks.failOnIssues = value
        }

    fun application(configure: Action<ApplicationSpec>) {
        defaultChecks.application(configure)
    }

    fun platform(configure: Action<PlatformSpec>) {
        defaultChecks.platform(configure)
    }

    fun ignore(configure: Action<IgnoreSpec>) {
        defaultChecks.ignore(configure)
    }

    private fun Project.sourceSet(sourceSet: String) = extensions.getByType<SourceSetContainer>().getByName(sourceSet)

    fun check(name: String, configure: Action<ExpediterCheckSpec>) {
        configure.execute(check(name))
    }

    private fun ExpediterTask.configureApplicationClasses(spec: ApplicationSpec) {
        for (conf in spec.configurations) {
            artifactCollection(selector.artifacts(conf))
        }

        for (file in spec.files) {
            files.from(file)
        }

        for (sourceSetName in spec.sourceSets) {
            val sourceSet = project.sourceSet(sourceSetName)

            for (dir in sourceSet.output.classesDirs.files) {
                projectOutputDirs.add(project.layout.projectDirectory.dir(dir.path))
            }

            dependsOn(sourceSet.classesTaskName)
        }
    }

    private fun ExpediterTask.configurePlatformClasses(spec: PlatformSpec) {
        jvmVersion = spec.jvmVersion

        val expediterConfigurations = spec.expediterConfigurations.toMutableList()

        spec.androidSpec.run {
            if (sdk != null) {
                val config = project.configurations.create("_expediter_type_descriptors_")
                project.dependencies.add(config.name, artifact())
                expediterConfigurations.add(config.name)
            }
        }

        for (conf in expediterConfigurations) {
            typeDescriptors.from(project.configurations.getByName(conf))
        }

        for (conf in spec.animalSnifferConfigurations) {
            animalSnifferSignatures.from(project.configurations.getByName(conf))
        }

        if (spec.jvmVersion != null && spec.androidSpec.sdk != null) {
            throw GradleException("Both jvmVersion and android.sdk are set. Configure multiple checks instead.")
        }

        for (conf in spec.configurations) {
            platformArtifactCollection(selector.artifacts(conf))
        }
    }

    private fun check(name: String) = specs.computeIfAbsent(CheckKey(name)) { key ->
        project.objects.newInstance<ExpediterCheckSpec>().also { spec ->
            val task = project.tasks.register<ExpediterTask>(key.taskName) {
                usesService(sharedCache)
                cache.set(sharedCache)

                configureApplicationClasses(spec.application.orDefaultIfEmpty())
                configurePlatformClasses(spec.platform)

                ignore = spec.ignoreSpec.buildIgnore()

                ignoreFiles.from(spec.ignoreSpec.files)

                report = project.layout.buildDirectory.file("${key.reportName}.json").get().asFile

                failOnIssues = spec.failOnIssues

                roots = spec.application.rootSelectorSpec.type
            }

            project.configureAndroidOutputs(task, selector) { spec.application.androidSpec }
        }
    }

    @JvmInline
    private value class CheckKey(val check: String) {
        val taskName: String get() = "expediter" + check.replaceFirstChar(Char::titlecase)
        val reportName: String get() = if (check == "default") {
            "expediter"
        } else {
            "expediter-$check"
        }
    }
}
