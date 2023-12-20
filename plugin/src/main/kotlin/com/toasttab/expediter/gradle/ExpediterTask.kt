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

import com.toasttab.expediter.Expediter
import com.toasttab.expediter.gradle.service.ApplicationTypeCache
import com.toasttab.expediter.ignore.Ignore
import com.toasttab.expediter.issue.IssueReport
import com.toasttab.expediter.parser.TypeParsers
import com.toasttab.expediter.provider.InMemoryPlatformTypeProvider
import com.toasttab.expediter.provider.JvmTypeProvider
import com.toasttab.expediter.provider.PlatformClassloaderTypeProvider
import com.toasttab.expediter.provider.PlatformTypeProvider
import com.toasttab.expediter.provider.PlatformTypeProviderChain
import com.toasttab.expediter.scanner.ClasspathScanner
import com.toasttab.expediter.sniffer.AnimalSnifferParser
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.artifacts.ArtifactCollection
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import protokt.v1.toasttab.expediter.v1.TypeDescriptors
import java.io.File
import java.util.zip.GZIPInputStream

@CacheableTask
abstract class ExpediterTask : DefaultTask() {
    private val applicationConfigurationArtifacts = mutableListOf<ArtifactCollection>()
    private val platformConfigurationArtifacts = mutableListOf<ArtifactCollection>()

    @get:Internal
    abstract val cache: Property<ApplicationTypeCache>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.ABSOLUTE)
    val applicationArtifacts get() = applicationConfigurationArtifacts.asFileCollection()

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.ABSOLUTE)
    val platformArtifacts get() = platformConfigurationArtifacts.asFileCollection()

    private fun Collection<ArtifactCollection>.asFileCollection() = if (isEmpty()) {
        project.objects.fileCollection()
    } else {
        map { it.artifactFiles }.reduce(FileCollection::plus)
    }

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.ABSOLUTE)
    abstract val files: ConfigurableFileCollection

    fun artifactCollection(artifactCollection: ArtifactCollection) {
        applicationConfigurationArtifacts.add(artifactCollection)
    }

    fun platformArtifactCollection(artifactCollection: ArtifactCollection) {
        platformConfigurationArtifacts.add(artifactCollection)
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

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val typeDescriptors: ConfigurableFileCollection

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val ignoreFiles: ConfigurableFileCollection

    @Input
    var failOnIssues: Boolean = false

    @TaskAction
    fun execute() {
        val providers = mutableListOf<PlatformTypeProvider>()

        jvmVersion?.let {
            providers.add(JvmTypeProvider.forTarget(it))
        }

        if (!platformArtifacts.isEmpty) {
            providers.add(
                InMemoryPlatformTypeProvider(
                    ClasspathScanner(platformArtifacts).scan { i, _ -> TypeParsers.typeDescriptor(i) }
                )
            )
        }

        for (signaturesFile in animalSnifferSignatures) {
            signaturesFile.inputStream().buffered().use {
                providers.add(InMemoryPlatformTypeProvider(AnimalSnifferParser.parse(it)))
            }
        }

        for (descriptorFile in typeDescriptors) {
            GZIPInputStream(descriptorFile.inputStream().buffered()).use {
                providers.add(InMemoryPlatformTypeProvider(TypeDescriptors.deserialize(it).types))
            }
        }

        if (jvmVersion == null && animalSnifferSignatures.isEmpty && typeDescriptors.isEmpty && platformArtifacts.isEmpty) {
            logger.warn("No platform APIs specified, falling back to the platform classloader of the current JVM.")

            providers.add(PlatformClassloaderTypeProvider)
        }

        val ignores = ignoreFiles.flatMap {
            it.inputStream().buffered().use {
                IssueReport.fromJson(it).issues
            }
        }.toSet()

        val issues = Expediter(
            ignore,
            cache.get().resolve(applicationArtifacts + files),
            PlatformTypeProviderChain(providers)
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
