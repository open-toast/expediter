/*
 * Copyright (c) 2024 Toast Inc.
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
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.toasttab.expediter.Expediter
import com.toasttab.expediter.ignore.Ignore
import com.toasttab.expediter.issue.IssueOrder
import com.toasttab.expediter.issue.IssueReport
import com.toasttab.expediter.provider.ClasspathApplicationTypesProvider
import com.toasttab.expediter.provider.InMemoryPlatformTypeProvider
import com.toasttab.expediter.provider.JvmTypeProvider
import com.toasttab.expediter.provider.PlatformTypeProvider
import com.toasttab.expediter.roots.RootSelector
import com.toasttab.expediter.types.ClassfileSource
import com.toasttab.expediter.types.ClassfileSourceType
import protokt.v1.toasttab.expediter.v1.TypeDescriptors
import java.io.File
import java.util.zip.GZIPInputStream

class ExpediterCliCommand : CliktCommand() {
    val output: String by option().required()
    val projectClasses: List<String> by option().multiple()
    val libraries: List<String> by option().multiple()
    val ignoresFiles: List<String> by option().multiple()
    val jvmPlatform: String? by option()
    val platformDescriptors: String? by option()
    val projectName: String by option().default("project")

    private fun ignores() = ignoresFiles.flatMap {
        File(it).inputStream().use {
            IssueReport.fromJson(it).issues
        }
    }.toSet()

    private fun appTypes() = ClasspathApplicationTypesProvider(
        projectClasses.map { ClassfileSource(File(it), ClassfileSourceType.SOURCE_SET, it) } +
            libraries.map { ClassfileSource(File(it), ClassfileSourceType.EXTERNAL_DEPENDENCY, it) }
    )

    fun platform(): PlatformTypeProvider {
        val jvm = jvmPlatform?.let(String::toInt)
        val platformFile = platformDescriptors?.let(::File)

        return if (jvm != null) {
            JvmTypeProvider.forTarget(jvm)
        } else if (platformFile != null) {
            InMemoryPlatformTypeProvider(
                GZIPInputStream(platformFile.inputStream().buffered()).use {
                    TypeDescriptors.deserialize(it)
                }.types
            )
        } else {
            error("Must specify either jvm version or platform descriptors")
        }
    }
    override fun run() {
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

        File(output).outputStream().buffered().use {
            issueReport.toJson(it)
        }
    }
}

fun main(args: Array<String>) = ExpediterCliCommand().main(args)
