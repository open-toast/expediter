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
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
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

enum class CliMode {
    CHECK,
    PRINT,
    DESCRIBE
}

class ExpediterCliCommand(
    private val stdout: PrintStream = System.out
) : CliktCommand() {
    val mode: CliMode by option(help = "Mode of operation").enum<CliMode>(ignoreCase = true).default(CliMode.CHECK)
    val output: String? by option(help = "Output file for the issue report (check mode)")
    val projectClasses: List<String> by option().multiple()
    val libraries: List<String> by option().multiple()
    val ignoresFiles: List<String> by option().multiple()
    val jvmPlatform: String? by option()
    val platformDescriptors: String? by option(help = "Gzipped TypeDescriptors proto file; also the input for print mode")
    val projectName: String by option().default("project")

    private fun ignores() = ignoresFiles.flatMap {
        File(it).inputStream().use {
            IssueReport.fromJson(it).issues
        }
    }.toSet()

    private fun appTypes() = ClasspathApplicationTypesProvider(
        projectClasses.map { ClassfileSource(File(it), ClassfileSourceType.PROJECT) } +
            libraries.map { ClassfileSource(File(it), ClassfileSourceType.EXTERNAL_DEPENDENCY) }
    )

    private fun readPlatformDescriptors(file: File): TypeDescriptors =
        GZIPInputStream(file.inputStream().buffered()).use {
            TypeDescriptors.deserialize(it)
        }

    fun platform(): PlatformTypeProvider {
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

    private fun runCheck() {
        val outputPath = output ?: error("--output is required in check mode")

        val issues = Expediter(
            ignore = Ignore.SpecificIssues(ignores()),
            appTypes = appTypes(),
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

        File(outputPath).outputStream().buffered().use {
            issueReport.toJson(it)
        }
    }

    private fun runPrint() {
        val file = platformDescriptors?.let(::File)
            ?: error("--platform-descriptors is required in print mode")

        val descriptors = readPlatformDescriptors(file)

        SignaturePrinter.print(descriptors, stdout)
    }

    private fun runDescribe() {
        val outputPath = output ?: error("--output is required in describe mode")

        val sources = projectClasses.map { ClassfileSource(File(it), ClassfileSourceType.PROJECT) } +
            libraries.map { ClassfileSource(File(it), ClassfileSourceType.EXTERNAL_DEPENDENCY) }

        if (sources.isEmpty()) {
            error("Must specify at least one --project-classes or --libraries in describe mode")
        }

        val types = ClasspathScanner(sources)
            .scan { stream, _ -> TypeParsers.typeDescriptor(stream) }
            .sortedBy { it.name }

        val descriptors = TypeDescriptors {
            description = projectName
            this.types = types
        }

        File(outputPath).outputStream().buffered().use { fileOut ->
            GZIPOutputStream(fileOut).use { gzip ->
                descriptors.serialize(gzip)
            }
        }
    }

    override fun run() {
        when (mode) {
            CliMode.CHECK -> runCheck()
            CliMode.PRINT -> runPrint()
            CliMode.DESCRIBE -> runDescribe()
        }
    }
}

fun main(args: Array<String>) = ExpediterCliCommand().main(args)
