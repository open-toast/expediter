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

package com.toasttab.expediter.gradle

import com.toasttab.expediter.issue.Issue
import com.toasttab.expediter.issue.IssueReport
import com.toasttab.expediter.types.MemberAccess
import com.toasttab.expediter.types.MemberSymbolicReference
import com.toasttab.expediter.types.MethodAccessType
import com.toasttab.gradle.testkit.GradleVersion
import com.toasttab.gradle.testkit.ParameterizedWithGradleVersions
import com.toasttab.gradle.testkit.Property
import com.toasttab.gradle.testkit.TestKit
import com.toasttab.gradle.testkit.TestProject
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.filterIsInstance
import strikt.assertions.isEmpty
import kotlin.io.path.readText

// The Kotlin Gradle plugin only supports a bounded range of Gradle versions, so each
// Gradle version under test is paired with a compatible Kotlin version via the
// KOTLIN_VERSION replacement token (see the testkitTests block in build.gradle.kts).
// Kotlin 2.2.x supports Gradle up to 8.14; Gradle 9.x requires Kotlin >= 2.3, and the
// latest Kotlin (2.4.x, the project default) supports Gradle up to 9.5.
private const val KOTLIN_FOR_GRADLE_8 = "2.2.21"

@TestKit(
    versions = [
        GradleVersion("8.6", properties = [Property("KOTLIN_VERSION", KOTLIN_FOR_GRADLE_8)]),
        GradleVersion("8.14.1", properties = [Property("KOTLIN_VERSION", KOTLIN_FOR_GRADLE_8)]),
        GradleVersion("9.5.0")
    ]
)
class ExpediterPluginIntegrationTest {
    @ParameterizedWithGradleVersions
    fun `android compat`(project: TestProject) {
        project.buildAndFail("check")

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

    @ParameterizedWithGradleVersions
    fun `android compat animal sniffer`(project: TestProject) {
        project.buildAndFail("check")

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

    @ParameterizedWithGradleVersions
    fun `android compat source only`(project: TestProject) {
        project.buildAndFail("check")

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

    @ParameterizedWithGradleVersions
    fun `jvm compat`(project: TestProject) {
        project.buildAndFail("check")

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

    @ParameterizedWithGradleVersions
    fun `kotlin jvm compat`(project: TestProject) {
        project.buildAndFail("check")

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

    @ParameterizedWithGradleVersions
    fun `protokt android compat`(project: TestProject) {
        project.buildAndFail("check")

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

    @ParameterizedWithGradleVersions
    fun `multi check`(project: TestProject) {
        project.build("check")

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

    @ParameterizedWithGradleVersions
    fun multimodule(project: TestProject) {
        project.buildAndFail("app:expedite")

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

    @ParameterizedWithGradleVersions
    fun `cross library`(project: TestProject) {
        project.buildAndFail("check")

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

    @ParameterizedWithGradleVersions
    fun `cross library all roots`(project: TestProject) {
        project.buildAndFail("check")

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

    // AGP 8.5.2 (the oldest AGP 8 supported by the latest Kotlin) requires Gradle 8.7+,
    // so this test overrides the class-level versions, which start at 8.6.
    @ParameterizedWithGradleVersions(
        versions = [
            GradleVersion("8.7", properties = [Property("KOTLIN_VERSION", KOTLIN_FOR_GRADLE_8)]),
            GradleVersion("8.14.1", properties = [Property("KOTLIN_VERSION", KOTLIN_FOR_GRADLE_8)]),
            GradleVersion("9.5.0")
        ]
    )
    fun `android lib agp8`(project: TestProject) {
        project.buildAndFail("check")

        val report = IssueReport.fromJson(project.dir.resolve("build/expediter.json").readText())

        expectThat(report.issues).contains(
            Issue.MissingType("kotlin/io/path/CopyActionContext", "java/nio/file/Path")
        )

        expectThat(report.issues).filterIsInstance<Issue.DuplicateType>().isEmpty()
    }

    @ParameterizedWithGradleVersions(versions = [GradleVersion("9.3.0")])
    fun `android lib agp9`(project: TestProject) {
        project.buildAndFail("check")

        val report = IssueReport.fromJson(project.dir.resolve("build/expediter.json").readText())

        expectThat(report.issues).contains(
            Issue.MissingType("kotlin/io/path/CopyActionContext", "java/nio/file/Path")
        )

        expectThat(report.issues).filterIsInstance<Issue.DuplicateType>().isEmpty()
    }

    @ParameterizedWithGradleVersions(
        versions = [
            GradleVersion("8.5", properties = [Property("KOTLIN_VERSION", KOTLIN_FOR_GRADLE_8)]),
            GradleVersion("8.13", properties = [Property("KOTLIN_VERSION", KOTLIN_FOR_GRADLE_8)]),
            GradleVersion("8.14.1", properties = [Property("KOTLIN_VERSION", KOTLIN_FOR_GRADLE_8)]),
            GradleVersion("9.3.0")
        ]
    )
    fun `multiple outputs`(project: TestProject) {
        project.build("check")
    }

    @ParameterizedWithGradleVersions
    fun `ignore`(project: TestProject) {
        project.build("check")
    }

    @ParameterizedWithGradleVersions
    fun `ignore file`(project: TestProject) {
        project.build("check")
    }
}
