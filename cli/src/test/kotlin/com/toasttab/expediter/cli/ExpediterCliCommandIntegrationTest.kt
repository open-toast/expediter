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

import com.toasttab.expediter.issue.Issue
import com.toasttab.expediter.issue.IssueReport
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import strikt.api.expectThat
import strikt.assertions.contains
import java.io.File
import java.nio.file.Path
import kotlin.io.path.inputStream

class ExpediterCliCommandIntegrationTest {
    @TempDir
    lateinit var dir: Path

    @Test
    fun `run on self`() {
        val output = dir.resolve("expediter.json")

        ExpediterCliCommand().main(
            System.getProperty("libraries").split(File.pathSeparatorChar).flatMap {
                listOf("--libraries", it)
            } + listOf(
                "--project-classes", System.getProperty("classes"),
                "--output", output.toString(),
                "--jvm-platform", "11"
            )
        )

        val report = output.inputStream().use {
            IssueReport.fromJson(it)
        }

        expectThat(report.issues).contains(
            Issue.MissingType(
                caller = "com/github/ajalt/mordant/internal/nativeimage/WinKernel32Lib",
                target = "org/graalvm/word/PointerBase"
            )
        )
    }
}
