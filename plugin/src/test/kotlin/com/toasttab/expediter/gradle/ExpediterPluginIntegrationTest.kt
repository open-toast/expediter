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

import com.toasttab.expediter.issue.Issue
import com.toasttab.expediter.issue.IssueReport
import com.toasttab.expediter.types.MemberAccess
import com.toasttab.expediter.types.MemberSymbolicReference
import com.toasttab.expediter.types.MethodAccessType
import com.toasttab.gradle.testkit.TestKit
import com.toasttab.gradle.testkit.TestProject
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.containsExactlyInAnyOrder
import kotlin.io.path.readText

@TestKit
class ExpediterPluginIntegrationTest {
    @Test
    fun `android compat`(project: TestProject) {
        project.createRunner()
            .withArguments("check")
            .buildAndFail()

        val report = IssueReport.fromJson(project.dir.resolve("build/expediter.json").readText())

        expectThat(report.issues).containsExactlyInAnyOrder(
            Issue.MissingMember(
                "test/Caller",
                MemberAccess.MethodAccess(
                    "java/util/concurrent/ConcurrentHashMap",
                    null,
                    MemberSymbolicReference.MethodSymbolicReference(
                        "computeIfAbsent",
                        "(Ljava/lang/Object;Ljava/util/function/Function;)Ljava/lang/Object;"
                    ),
                    MethodAccessType.VIRTUAL
                )
            )
        )
    }

    @Test
    fun `jvm compat`(project: TestProject) {
        project.createRunner()
            .withArguments("check")
            .buildAndFail()

        val report = IssueReport.fromJson(project.dir.resolve("build/expediter.json").readText())

        expectThat(report.issues).containsExactlyInAnyOrder(
            Issue.MissingMember(
                "test/Caller",
                MemberAccess.MethodAccess(
                    "java/lang/String",
                    null,
                    MemberSymbolicReference.MethodSymbolicReference(
                        "isBlank",
                        "()Z"
                    ),
                    MethodAccessType.VIRTUAL
                )
            )
        )
    }

    @Test
    fun multimodule(project: TestProject) {
        project.createRunner().withArguments("app:expedite").buildAndFail()

        val report = IssueReport.fromJson(project.dir.resolve("app/build/expediter.json").readText())

        expectThat(report.issues).containsExactlyInAnyOrder(
            Issue.MissingMember(
                "test/A",
                MemberAccess.MethodAccess(
                    "java/lang/String",
                    null,
                    MemberSymbolicReference.MethodSymbolicReference(
                        "isBlank",
                        "()Z"
                    ),
                    MethodAccessType.VIRTUAL
                )
            ),

            Issue.MissingMember(
                "test/B",
                MemberAccess.MethodAccess(
                    "java/lang/String",
                    null,
                    MemberSymbolicReference.MethodSymbolicReference(
                        "isBlank",
                        "()Z"
                    ),
                    MethodAccessType.VIRTUAL
                )
            )
        )
    }

    @Test
    fun `cross library`(project: TestProject) {
        project.createRunner().withArguments("check").buildAndFail()

        val report = IssueReport.fromJson(project.dir.resolve("build/expediter.json").readText())

        expectThat(report.issues).contains(
            Issue.MissingMember(
                "com/fasterxml/jackson/databind/deser/BeanDeserializer",
                MemberAccess.MethodAccess(
                    "com/fasterxml/jackson/core/JsonParser",
                    null,
                    MemberSymbolicReference.MethodSymbolicReference(
                        "streamReadConstraints",
                        "()Lcom/fasterxml/jackson/core/StreamReadConstraints;"
                    ),
                    MethodAccessType.VIRTUAL
                )
            )
        )
    }

    @Test
    fun `android lib`(project: TestProject) {
        project.createRunner().withArguments("check").buildAndFail()

        val report = IssueReport.fromJson(project.dir.resolve("build/expediter.json").readText())

        expectThat(report.issues).contains(
            Issue.MissingType("kotlin/io/path/DirectoryEntriesReader", "java/nio/file/Files")
        )
    }
}
