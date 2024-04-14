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

package com.toasttab.expediter.test

import com.toasttab.expediter.Expediter
import com.toasttab.expediter.ignore.Ignore
import com.toasttab.expediter.issue.Issue
import com.toasttab.expediter.provider.ClasspathApplicationTypesProvider
import com.toasttab.expediter.provider.PlatformClassloaderTypeProvider
import com.toasttab.expediter.types.ClassfileSource
import com.toasttab.expediter.types.ClassfileSourceType
import com.toasttab.expediter.types.FieldAccessType
import com.toasttab.expediter.types.MemberAccess
import com.toasttab.expediter.types.MemberSymbolicReference
import com.toasttab.expediter.types.MethodAccessType
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import java.io.File

/**
 * We emulate a scenario where this project is compiled against lib1 + base but is only shipped with lib2.
 */
class ExpediterIntegrationTest {
    @Test
    fun integrate() {
        val testClasspath = System.getProperty("test-classpath")
        val scanner = ClasspathApplicationTypesProvider(
            testClasspath.split(':').map { ClassfileSource(File(it), ClassfileSourceType.UNKNOWN, it) }
        )
        val issues = Expediter(Ignore.NOTHING, scanner, PlatformClassloaderTypeProvider).findIssues()

        expectThat(issues).containsExactlyInAnyOrder(
            Issue.MissingMember(
                "com/toasttab/expediter/test/caller/Caller",
                MemberAccess.MethodAccess(
                    "com/toasttab/expediter/test/Bar",
                    null,
                    MemberSymbolicReference("bar", "(Ljava/lang/String;)V"),
                    MethodAccessType.VIRTUAL
                )
            ),

            Issue.MissingMember(
                "com/toasttab/expediter/test/caller/Caller",
                MemberAccess.MethodAccess(
                    "com/toasttab/expediter/test/Base",
                    null,
                    MemberSymbolicReference("supersuper", "()V"),
                    MethodAccessType.SPECIAL
                )
            ),

            Issue.AccessInstanceMemberStatically(
                "com/toasttab/expediter/test/caller/Caller",
                MemberAccess.MethodAccess(
                    "com/toasttab/expediter/test/Bar",
                    "com/toasttab/expediter/test/Bar",
                    MemberSymbolicReference("bar", "()V"),
                    MethodAccessType.STATIC
                )
            ),

            Issue.AccessInaccessibleMember(
                "com/toasttab/expediter/test/caller/Caller",
                MemberAccess.MethodAccess(
                    "com/toasttab/expediter/test/Bar",
                    "com/toasttab/expediter/test/Bar",
                    MemberSymbolicReference("bar", "(I)V"),
                    MethodAccessType.VIRTUAL
                )
            ),

            Issue.AccessInaccessibleMember(
                "com/toasttab/expediter/test/caller/Caller",
                MemberAccess.MethodAccess(
                    "com/toasttab/expediter/test/Bar",
                    "com/toasttab/expediter/test/Bar",
                    MemberSymbolicReference("bar", "(J)V"),
                    MethodAccessType.VIRTUAL
                )
            ),

            Issue.AccessInaccessibleMember(
                "com/toasttab/expediter/test/caller/Caller",
                MemberAccess.MethodAccess(
                    "com/toasttab/expediter/test/Baz",
                    "com/toasttab/expediter/test/Bar",
                    MemberSymbolicReference("bar", "(F)V"),
                    MethodAccessType.VIRTUAL
                )
            ),

            Issue.MissingMember(
                "com/toasttab/expediter/test/caller/Caller",
                MemberAccess.FieldAccess(
                    "com/toasttab/expediter/test/Baz",
                    null,
                    MemberSymbolicReference("a", "Ljava/lang/String;"),
                    FieldAccessType.INSTANCE
                )
            ),

            Issue.AccessInstanceMemberStatically(
                "com/toasttab/expediter/test/caller/Caller",
                MemberAccess.FieldAccess(
                    "com/toasttab/expediter/test/Baz",
                    "com/toasttab/expediter/test/Baz",
                    MemberSymbolicReference("x", "I"),
                    FieldAccessType.STATIC
                )
            ),

            Issue.AccessStaticMemberNonStatically(
                "com/toasttab/expediter/test/caller/Caller",
                MemberAccess.FieldAccess(
                    "com/toasttab/expediter/test/Baz",
                    "com/toasttab/expediter/test/Baz",
                    MemberSymbolicReference("y", "I"),
                    FieldAccessType.INSTANCE
                )
            ),

            Issue.AccessInaccessibleMember(
                "com/toasttab/expediter/test/caller/Caller",
                MemberAccess.FieldAccess(
                    "com/toasttab/expediter/test/Baz",
                    "com/toasttab/expediter/test/Baz",
                    MemberSymbolicReference("z", "I"),
                    FieldAccessType.INSTANCE
                )
            ),

            Issue.AccessInaccessibleMember(
                "com/toasttab/expediter/test/caller/Caller",
                MemberAccess.FieldAccess(
                    "com/toasttab/expediter/test/Bar",
                    "com/toasttab/expediter/test/BaseBar",
                    MemberSymbolicReference("j", "I"),
                    FieldAccessType.INSTANCE
                )
            ),

            Issue.MissingApplicationSuperType(
                "com/toasttab/expediter/test/Foo",
                setOf("com/toasttab/expediter/test/BaseFoo")
            ),

            Issue.DuplicateType(
                "com/toasttab/expediter/test/Dupe",
                listOf("testFixtures", "lib2-test-fixtures.jar")
            ),

            Issue.FinalApplicationSuperType(
                "com/toasttab/expediter/test/caller/Caller",
                setOf("com/toasttab/expediter/test/Base")
            ),

            Issue.MissingType(
                "com/toasttab/expediter/test/caller/Caller",
                "com/toasttab/expediter/test/Var"
            ),

            Issue.MissingApplicationSuperType(
                "com/toasttab/expediter/test/caller/VarVar",
                setOf("com/toasttab/expediter/test/Var")
            ),

            Issue.MissingType(
                "com/toasttab/expediter/test/caller/Caller",
                "com/toasttab/expediter/test/Ex"
            ),

            Issue.MissingType(
                "com/toasttab/expediter/test/caller/Caller",
                "com/toasttab/expediter/test/Param"
            ),

            Issue.MissingType(
                "com/toasttab/expediter/test/caller/Caller",
                "com/toasttab/expediter/test/ParamParam"
            ),

            Issue.MissingType(
                "com/toasttab/expediter/test/Bar",
                "com/toasttab/expediter/test/Param"
            ),

            Issue.MissingType(
                "com/toasttab/expediter/test/caller/Caller",
                "com/toasttab/expediter/test/Lambda"
            )
        )

        expectThat(issues.map { "$it" }).containsExactlyInAnyOrder(
            "com/toasttab/expediter/test/Bar refers to missing type com/toasttab/expediter/test/Param",
            "com/toasttab/expediter/test/caller/VarVar extends missing type com/toasttab/expediter/test/Var",
            "com/toasttab/expediter/test/caller/Caller extends final type com/toasttab/expediter/test/Base",
            "com/toasttab/expediter/test/caller/Caller refers to missing type com/toasttab/expediter/test/Ex",
            "com/toasttab/expediter/test/caller/Caller refers to missing type com/toasttab/expediter/test/Param",
            "com/toasttab/expediter/test/caller/Caller refers to missing type com/toasttab/expediter/test/ParamParam",
            "com/toasttab/expediter/test/caller/Caller refers to missing type com/toasttab/expediter/test/Var",
            "com/toasttab/expediter/test/caller/Caller refers to missing type com/toasttab/expediter/test/Lambda",
            "com/toasttab/expediter/test/caller/Caller accesses instance com/toasttab/expediter/test/Bar.bar()V statically",
            "com/toasttab/expediter/test/caller/Caller accesses static com/toasttab/expediter/test/Baz.yI non-statically",
            "com/toasttab/expediter/test/caller/Caller accesses inaccessible com/toasttab/expediter/test/Baz.zI",
            "com/toasttab/expediter/test/caller/Caller accesses inaccessible com/toasttab/expediter/test/BaseBar.jI (via com/toasttab/expediter/test/Bar)",
            "com/toasttab/expediter/test/caller/Caller accesses missing com/toasttab/expediter/test/Baz.aLjava/lang/String;",
            "com/toasttab/expediter/test/caller/Caller accesses instance com/toasttab/expediter/test/Baz.xI statically",
            "com/toasttab/expediter/test/caller/Caller accesses missing com/toasttab/expediter/test/Base.supersuper()V",
            "com/toasttab/expediter/test/caller/Caller accesses missing com/toasttab/expediter/test/Bar.bar(Ljava/lang/String;)V",
            "com/toasttab/expediter/test/caller/Caller accesses inaccessible com/toasttab/expediter/test/Bar.bar(F)V (via com/toasttab/expediter/test/Baz)",
            "com/toasttab/expediter/test/caller/Caller accesses inaccessible com/toasttab/expediter/test/Bar.bar(I)V",
            "com/toasttab/expediter/test/caller/Caller accesses inaccessible com/toasttab/expediter/test/Bar.bar(J)V",
            "com/toasttab/expediter/test/Foo extends missing type com/toasttab/expediter/test/BaseFoo",
            "duplicate class com/toasttab/expediter/test/Dupe in [testFixtures, lib2-test-fixtures.jar]"
        )
    }
}
