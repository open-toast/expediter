package com.toasttab.expediter.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.toasttab.expediter.Expediter
import com.toasttab.expediter.ignore.Ignore
import com.toasttab.expediter.issue.IssueReport
import com.toasttab.expediter.provider.ClasspathApplicationTypesProvider
import com.toasttab.expediter.provider.InMemoryPlatformTypeProvider
import com.toasttab.expediter.provider.JvmTypeProvider
import com.toasttab.expediter.provider.PlatformTypeProvider
import com.toasttab.expediter.roots.RootSelector
import com.toasttab.expediter.types.ApplicationType
import com.toasttab.expediter.types.ClassfileSource
import com.toasttab.expediter.types.ClassfileSourceType
import protokt.v1.toasttab.expediter.v1.TypeDescriptors
import java.io.File

class ExpediterCliCommand : CliktCommand() {
    val output: String by option().required()
    val projectClasses: List<String> by option().multiple()
    val libraries: List<String> by option().multiple()
    val ignoresFiles: List<String> by option().multiple()
    val jvmPlatform: String? by option()
    val platformDescriptors: String? by option()
    val projectName: String by option().default("project")

    fun ignores() = ignoresFiles.flatMap {
        File(it).inputStream().use {
            IssueReport.fromJson(it).issues
        }
    }.toSet()

    fun appTypes() = ClasspathApplicationTypesProvider(
        projectClasses.map { ClassfileSource(File(it), ClassfileSourceType.SOURCE_SET, it) } +
            libraries.map { ClassfileSource(File(it), ClassfileSourceType.EXTERNAL_DEPENDENCY, it) }
    )

    fun platform(): PlatformTypeProvider {
        return if (jvmPlatform != null) {
            JvmTypeProvider.forTarget(jvmPlatform!!.toInt())
        } else if (platformDescriptors != null) {
            InMemoryPlatformTypeProvider(
                File(platformDescriptors!!).inputStream().use {
                    TypeDescriptors.deserialize(it)
                }.types
            )
        } else {
            error("blerg")
        }
    }
    override fun run() {
        val issues = Expediter(
            ignore = Ignore.NOTHING,
            appTypes = appTypes(),
            platformTypeProvider = platform(),
            rootSelector = object : RootSelector {
                override fun isRoot(type: ApplicationType) = type.source.type == ClassfileSourceType.SOURCE_SET || type.source.type == ClassfileSourceType.SUBPROJECT_DEPENDENCY
            }
        ).findIssues().subtract(
            ignores()
        )

        val issueReport = IssueReport(
            projectName,
            issues.sortedWith(
                compareBy(
                    { it.caller },
                    { it.target }
                )
            )
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
