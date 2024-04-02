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
import com.toasttab.gradle.testkit.ParameterizedWithGradleVersions
import com.toasttab.gradle.testkit.TestKit
import com.toasttab.gradle.testkit.TestProject
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.filterIsInstance
import strikt.assertions.isEmpty
import kotlin.io.path.readText

@TestKit(gradleVersions = ["8.6", "8.7"])
class ExpediterPluginIntegrationTest {
    @Test
    @ParameterizedWithGradleVersions
    fun `android compat`(project: TestProject) {
        project.createRunner()
            .withArguments("check")
            .buildAndFail()

        val report = IssueReport.fromJson(project.dir.resolve("build/expediter.json").readText())

        expectThat(report.issues).contains(
            Issue.MissingMember(
                "test/Caller",
                MemberAccess.MethodAccess(
                    "java/util/concurrent/ConcurrentHashMap",
                    null,
                    MemberSymbolicReference(
                        "computeIfAbsent",
                        "(Ljava/lang/Object;Ljava/util/function/Function;)Ljava/lang/Object;"
                    ),
                    MethodAccessType.VIRTUAL
                )
            ),

            Issue.MissingType(
                "com/fasterxml/jackson/databind/introspect/POJOPropertyBuilder",
                "java/util/stream/Collectors"
            )
        )
    }

    @Test
    @ParameterizedWithGradleVersions
    fun `android compat animal sniffer`(project: TestProject) {
        project.createRunner()
            .withArguments("check")
            .buildAndFail()

        val report = IssueReport.fromJson(project.dir.resolve("build/expediter.json").readText())

        expectThat(report.issues).contains(
            Issue.MissingMember(
                "test/Caller",
                MemberAccess.MethodAccess(
                    "java/util/concurrent/ConcurrentHashMap",
                    null,
                    MemberSymbolicReference(
                        "computeIfAbsent",
                        "(Ljava/lang/Object;Ljava/util/function/Function;)Ljava/lang/Object;"
                    ),
                    MethodAccessType.VIRTUAL
                )
            ),

            Issue.MissingType(
                "com/fasterxml/jackson/databind/introspect/POJOPropertyBuilder",
                "java/util/stream/Collectors"
            )
        )
    }

    @Test
    @ParameterizedWithGradleVersions
    fun `android compat source only`(project: TestProject) {
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
                    MemberSymbolicReference(
                        "computeIfAbsent",
                        "(Ljava/lang/Object;Ljava/util/function/Function;)Ljava/lang/Object;"
                    ),
                    MethodAccessType.VIRTUAL
                )
            ),

            Issue.MissingType("test/Caller", "java/util/function/Function")
        )
    }

    @Test
    @ParameterizedWithGradleVersions
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
                    MemberSymbolicReference(
                        "isBlank",
                        "()Z"
                    ),
                    MethodAccessType.VIRTUAL
                )
            )
        )
    }

    @Test
    @ParameterizedWithGradleVersions
    fun `kotlin jvm compat`(project: TestProject) {
        project.createRunner()
            .withArguments("check")
            .buildAndFail()

        val report = IssueReport.fromJson(project.dir.resolve("build/expediter.json").readText())

        expectThat(report.issues).contains(
            Issue.MissingMember(
                "test/Caller",
                MemberAccess.MethodAccess(
                    "java/lang/String",
                    null,
                    MemberSymbolicReference(
                        "isBlank",
                        "()Z"
                    ),
                    MethodAccessType.VIRTUAL
                )
            )
        )
    }

    @Test
    @ParameterizedWithGradleVersions
    fun `protokt android compat`(project: TestProject) {
        project.createRunner()
            .withArguments("check")
            .buildAndFail()

        val report = IssueReport.fromJson(project.dir.resolve("build/expediter.json").readText())

        expectThat(report.issues).contains(
            Issue.MissingMember(
                "com/google/protobuf/UnsafeUtil\$JvmMemoryAccessor",
                MemberAccess.MethodAccess(
                    "sun/misc/Unsafe",
                    null,
                    MemberSymbolicReference(
                        "getLong",
                        "(J)J"
                    ),
                    MethodAccessType.VIRTUAL
                )
            )
        )
    }

    @Test
    @ParameterizedWithGradleVersions
    fun `multi check`(project: TestProject) {
        project.createRunner().withArguments("check").build()

        val reportJvm = IssueReport.fromJson(project.dir.resolve("build/expediter-jvm.json").readText())
        val reportAndroid = IssueReport.fromJson(project.dir.resolve("build/expediter-android.json").readText())

        expectThat(reportJvm.issues).containsExactlyInAnyOrder(
            Issue.MissingMember(
                "test/Caller",
                MemberAccess.MethodAccess(
                    "java/lang/String",
                    null,
                    MemberSymbolicReference(
                        "isBlank",
                        "()Z"
                    ),
                    MethodAccessType.VIRTUAL
                )
            )
        )

        expectThat(reportAndroid.issues).containsExactlyInAnyOrder(
            Issue.MissingType(
                "test/Caller",
                "javax/management/Descriptor"
            )
        )
    }

    @Test
    @ParameterizedWithGradleVersions
    fun multimodule(project: TestProject) {
        project.createRunner().withArguments("app:expedite").buildAndFail()

        val report = IssueReport.fromJson(project.dir.resolve("app/build/expediter.json").readText())

        expectThat(report.issues).containsExactlyInAnyOrder(
            Issue.MissingMember(
                "test/A",
                MemberAccess.MethodAccess(
                    "java/lang/String",
                    null,
                    MemberSymbolicReference(
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
                    MemberSymbolicReference(
                        "isBlank",
                        "()Z"
                    ),
                    MethodAccessType.VIRTUAL
                )
            )
        )
    }

    @Test
    @ParameterizedWithGradleVersions
    fun `cross library`(project: TestProject) {
        project.createRunner().withArguments("check").buildAndFail()

        val report = IssueReport.fromJson(project.dir.resolve("build/expediter.json").readText())

        expectThat(report.issues).contains(
            Issue.MissingMember(
                "com/fasterxml/jackson/databind/deser/BeanDeserializer",
                MemberAccess.MethodAccess(
                    "com/fasterxml/jackson/core/JsonParser",
                    null,
                    MemberSymbolicReference(
                        "streamReadConstraints",
                        "()Lcom/fasterxml/jackson/core/StreamReadConstraints;"
                    ),
                    MethodAccessType.VIRTUAL
                )
            )
        )
    }

    @Test
    @ParameterizedWithGradleVersions
    fun `android lib`(project: TestProject) {
        project.createRunner().withArguments("check").buildAndFail()

        val report = IssueReport.fromJson(project.dir.resolve("build/expediter.json").readText())

        expectThat(report.issues).contains(
            Issue.MissingType("kotlin/io/path/DirectoryEntriesReader", "java/nio/file/Files")
        )

        expectThat(report.issues).filterIsInstance<Issue.DuplicateType>().isEmpty()
    }

    @Test
    @ParameterizedWithGradleVersions
    fun `ignore`(project: TestProject) {
        project.createRunner().withArguments("check").build()
    }

    @Test
    @ParameterizedWithGradleVersions
    fun `ignore file`(project: TestProject) {
        project.createRunner().withArguments("check").build()
    }
}
