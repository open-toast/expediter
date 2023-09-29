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

import com.toasttab.expediter.ClasspathScanner
import com.toasttab.expediter.Expediter
import com.toasttab.expediter.ignore.Ignore
import com.toasttab.expediter.issue.IssueReport
import com.toasttab.expediter.sniffer.AnimalSnifferParser
import com.toasttab.expediter.types.InMemoryPlatformTypeProvider
import com.toasttab.expediter.types.JvmTypeProvider
import com.toasttab.expediter.types.PlatformClassloaderTypeProvider
import com.toasttab.expediter.types.PlatformTypeProvider
import com.toasttab.expediter.types.PlatformTypeProviderChain
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.artifacts.ArtifactCollection
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File

@CacheableTask
abstract class ExpediterTask : DefaultTask() {
    private val configurationArtifacts = mutableListOf<ArtifactCollection>()

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.ABSOLUTE)
    val artifacts: FileCollection get() {
        return if (configurationArtifacts.isEmpty()) {
            project.objects.fileCollection()
        } else {
            configurationArtifacts.map {
                it.artifactFiles
            }.reduce(FileCollection::plus)
        }
    }

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.ABSOLUTE)
    abstract val files: ConfigurableFileCollection

    fun artifactCollection(artifactCollection: ArtifactCollection) {
        configurationArtifacts.add(artifactCollection)
    }

    @OutputFile
    lateinit var report: File

    @Input
    lateinit var ignore: Ignore

    @Input
    @Optional
    var jvmVersion: Int? = null

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val animalSnifferSignatures: ConfigurableFileCollection

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    @Optional
    var ignoreFile: File? = null

    @Input
    var failOnIssues: Boolean = false

    @TaskAction
    fun execute() {
        val providers = mutableListOf<PlatformTypeProvider>()

        jvmVersion?.let {
            providers.add(JvmTypeProvider.forTarget(it))
        }

        for (signaturesFile in animalSnifferSignatures) {
            signaturesFile.inputStream().buffered().use {
                providers.add(InMemoryPlatformTypeProvider(AnimalSnifferParser.parse(it)))
            }
        }

        if (jvmVersion == null && animalSnifferSignatures.isEmpty) {
            logger.warn("No platform APIs specified, falling back to the platform classloader of the current JVM.")

            providers.add(PlatformClassloaderTypeProvider)
        }

        val ignores = ignoreFile?.let {
            it.inputStream().buffered().use {
                IssueReport.fromJson(it)
            }.issues.toSet()
        } ?: setOf()

        val issues = Expediter(
            ignore,
            ClasspathScanner(artifacts + files),
            PlatformTypeProviderChain(
                providers
            )
        ).findIssues().subtract(ignores)

        for (issue in issues) {
            logger.warn("{}", issue)
        }

        val issueReport = IssueReport(project.name, issues.sortedBy { it.target })

        report.outputStream().buffered().use {
            issueReport.toJson(it)
        }

        if (failOnIssues && issueReport.issues.isNotEmpty()) {
            throw GradleException("Found compatibility issues, see $report")
        }
    }
}
