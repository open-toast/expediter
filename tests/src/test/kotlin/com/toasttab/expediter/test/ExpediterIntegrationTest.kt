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

import com.toasttab.expediter.ClasspathScanner
import com.toasttab.expediter.Expediter
import com.toasttab.expediter.ignore.Ignore
import com.toasttab.expediter.issue.Issue
import com.toasttab.expediter.types.FieldAccessType
import com.toasttab.expediter.types.MemberAccess
import com.toasttab.expediter.types.MemberSymbolicReference
import com.toasttab.expediter.types.MemberSymbolicReference.MethodSymbolicReference
import com.toasttab.expediter.types.MethodAccessType
import com.toasttab.expediter.types.PlatformClassloaderTypeProvider
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
        val scanner = ClasspathScanner(testClasspath.split(':').map { File(it) })
        val p = Expediter(Ignore.NOTHING, scanner, PlatformClassloaderTypeProvider).findIssues()

        expectThat(p).containsExactlyInAnyOrder(
            Issue.MissingMember(
                "com/toasttab/expediter/test/caller/Caller",
                MemberAccess.MethodAccess(
                    "com/toasttab/expediter/test/Bar",
                    null,
                    MethodSymbolicReference("bar", "(Ljava/lang/String;)V"),
                    MethodAccessType.VIRTUAL
                )
            ),

            Issue.AccessInstanceMemberStatically(
                "com/toasttab/expediter/test/caller/Caller",
                MemberAccess.MethodAccess(
                    "com/toasttab/expediter/test/Bar",
                    "com/toasttab/expediter/test/Bar",
                    MethodSymbolicReference("bar", "()V"),
                    MethodAccessType.STATIC
                )
            ),

            Issue.AccessInaccessibleMember(
                "com/toasttab/expediter/test/caller/Caller",
                MemberAccess.MethodAccess(
                    "com/toasttab/expediter/test/Bar",
                    "com/toasttab/expediter/test/Bar",
                    MethodSymbolicReference("bar", "(I)V"),
                    MethodAccessType.VIRTUAL
                )
            ),

            Issue.AccessInaccessibleMember(
                "com/toasttab/expediter/test/caller/Caller",
                MemberAccess.MethodAccess(
                    "com/toasttab/expediter/test/Bar",
                    "com/toasttab/expediter/test/Bar",
                    MethodSymbolicReference("bar", "(J)V"),
                    MethodAccessType.VIRTUAL
                )
            ),

            Issue.MissingMember(
                "com/toasttab/expediter/test/caller/Caller",
                MemberAccess.FieldAccess(
                    "com/toasttab/expediter/test/Baz",
                    null,
                    MemberSymbolicReference.FieldSymbolicReference("a", "Ljava/lang/String;"),
                    FieldAccessType.INSTANCE
                )
            ),

            Issue.AccessInstanceMemberStatically(
                "com/toasttab/expediter/test/caller/Caller",
                MemberAccess.FieldAccess(
                    "com/toasttab/expediter/test/Baz",
                    "com/toasttab/expediter/test/Baz",
                    MemberSymbolicReference.FieldSymbolicReference("x", "I"),
                    FieldAccessType.STATIC
                )
            ),

            Issue.AccessStaticMemberNonStatically(
                "com/toasttab/expediter/test/caller/Caller",
                MemberAccess.FieldAccess(
                    "com/toasttab/expediter/test/Baz",
                    "com/toasttab/expediter/test/Baz",
                    MemberSymbolicReference.FieldSymbolicReference("y", "I"),
                    FieldAccessType.INSTANCE
                )
            ),

            Issue.AccessInaccessibleMember(
                "com/toasttab/expediter/test/caller/Caller",
                MemberAccess.FieldAccess(
                    "com/toasttab/expediter/test/Baz",
                    "com/toasttab/expediter/test/Baz",
                    MemberSymbolicReference.FieldSymbolicReference("z", "I"),
                    FieldAccessType.INSTANCE
                )
            ),

            Issue.AccessInaccessibleMember(
                "com/toasttab/expediter/test/caller/Caller",
                MemberAccess.FieldAccess(
                    "com/toasttab/expediter/test/Bar",
                    "com/toasttab/expediter/test/BaseBar",
                    MemberSymbolicReference.FieldSymbolicReference("j", "I"),
                    FieldAccessType.INSTANCE
                )
            ),

            Issue.MissingSuperType(
                "com/toasttab/expediter/test/caller/Caller",
                "com/toasttab/expediter/test/Foo",
                setOf("com/toasttab/expediter/test/BaseFoo")
            ),

            Issue.MissingType(
                "com/toasttab/expediter/test/caller/Caller",
                "com/toasttab/expediter/test/BaseFoo"
            ),

            Issue.DuplicateType(
                "com/toasttab/expediter/test/Dupe",
                listOf("main", "lib2.jar")
            )
        )
    }
}
