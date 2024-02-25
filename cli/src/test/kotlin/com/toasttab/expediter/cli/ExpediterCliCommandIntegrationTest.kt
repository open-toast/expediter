package com.toasttab.expediter.cli

import com.toasttab.expediter.issue.IssueReport
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import kotlin.io.path.inputStream

class ExpediterCliCommandIntegrationTest {
    @TempDir
    lateinit var dir: Path
    @Test
    fun test() {
        val output = dir.resolve("expediter.json")

        ExpediterCliCommand().main(
            System.getProperty("libraries").split(File.pathSeparatorChar).flatMap {
                listOf("--libraries", it)
            } +  listOf(
                "--project-classes", System.getProperty("classes"),
                "--output", output.toString(),
                "--jvm-platform", "11"
            )
        )

        output.inputStream().use {
            IssueReport.fromJson(it)
        }
    }
}