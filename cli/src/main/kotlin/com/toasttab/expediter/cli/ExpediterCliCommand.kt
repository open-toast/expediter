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

package com.toasttab.expediter.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.toasttab.expediter.Expediter
import com.toasttab.expediter.ignore.Ignore
import com.toasttab.expediter.issue.IssueOrder
import com.toasttab.expediter.issue.IssueReport
import com.toasttab.expediter.parser.TypeParsers
import com.toasttab.expediter.provider.ClasspathApplicationTypesProvider
import com.toasttab.expediter.provider.InMemoryPlatformTypeProvider
import com.toasttab.expediter.provider.JvmTypeProvider
import com.toasttab.expediter.provider.PlatformTypeProvider
import com.toasttab.expediter.roots.RootSelector
import com.toasttab.expediter.scanner.ClasspathScanner
import com.toasttab.expediter.types.ClassfileSource
import com.toasttab.expediter.types.ClassfileSourceType
import protokt.v1.toasttab.expediter.v1.TypeDescriptors
import java.io.File
import java.io.PrintStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

class SourcesOptions : OptionGroup("Source Options") {
    val projectClasses: List<String> by option().multiple()
    val libraries: List<String> by option().multiple()

    fun sources(): List<ClassfileSource> =
        projectClasses.map { ClassfileSource(File(it), ClassfileSourceType.PROJECT) } +
            libraries.map { ClassfileSource(File(it), ClassfileSourceType.EXTERNAL_DEPENDENCY) }
}

private fun readPlatformDescriptors(file: File): TypeDescriptors =
    GZIPInputStream(file.inputStream().buffered()).use {
        TypeDescriptors.deserialize(it)
    }

class ExpediterCliCommand(
    stdout: PrintStream = System.out
) : CliktCommand() {
    init {
        subcommands(CheckCommand(), PrintCommand(stdout), DescribeCommand())
    }

    override fun run() = Unit
}

class CheckCommand : CliktCommand(name = "check") {
    private val sources by SourcesOptions()
    private val output: String by option(help = "Output file for the issue report").required()
    private val ignoresFiles: List<String> by option().multiple()
    private val jvmPlatform: String? by option()
    private val platformDescriptors: String? by option(help = "Gzipped TypeDescriptors proto file")
    private val projectName: String by option().default("project")

    private fun ignores() = ignoresFiles.flatMap {
        File(it).inputStream().use {
            IssueReport.fromJson(it).issues
        }
    }.toSet()

    private fun platform(): PlatformTypeProvider {
        val jvm = jvmPlatform?.let(String::toInt)
        val platformFile = platformDescriptors?.let(::File)

        return if (jvm != null) {
            JvmTypeProvider.forTarget(jvm)
        } else if (platformFile != null) {
            InMemoryPlatformTypeProvider(readPlatformDescriptors(platformFile).types)
        } else {
            error("Must specify either jvm version or platform descriptors")
        }
    }

    override fun run() {
        val issues = Expediter(
            ignore = Ignore.SpecificIssues(ignores()),
            appTypes = ClasspathApplicationTypesProvider(sources.sources()),
            platformTypeProvider = platform(),
            rootSelector = RootSelector.ProjectClasses
        ).findIssues()

        val issueReport = IssueReport(
            projectName,
            issues.sortedWith(IssueOrder.TYPE)
        )

        issueReport.issues.forEach {
            System.err.println(it)
        }

        File(output).outputStream().buffered().use {
            issueReport.toJson(it)
        }
    }
}

class PrintCommand(private val stdout: PrintStream) : CliktCommand(name = "print") {
    private val platformDescriptors: String by option(help = "Gzipped TypeDescriptors proto file").required()

    override fun run() {
        val descriptors = readPlatformDescriptors(File(platformDescriptors))
        SignaturePrinter.print(descriptors, stdout)
    }
}

class DescribeCommand : CliktCommand(name = "describe") {
    private val sources by SourcesOptions()
    private val output: String by option(help = "Output file for the type descriptors").required()
    private val projectName: String by option().default("project")

    override fun run() {
        val classfileSources = sources.sources()

        if (classfileSources.isEmpty()) {
            error("Must specify at least one --project-classes or --libraries")
        }

        val types = ClasspathScanner(classfileSources)
            .scan { stream, _ -> TypeParsers.typeDescriptor(stream) }
            .sortedBy { it.name }

        val descriptors = TypeDescriptors {
            description = projectName
            this.types = types
        }

        File(output).outputStream().buffered().use { fileOut ->
            GZIPOutputStream(fileOut).use { gzip ->
                descriptors.serialize(gzip)
            }
        }
    }
}

fun main(args: Array<String>) = ExpediterCliCommand().main(args)
