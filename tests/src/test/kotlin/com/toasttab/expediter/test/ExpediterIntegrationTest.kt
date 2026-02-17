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
            testClasspath.split(':').map { ClassfileSource(File(it), ClassfileSourceType.UNKNOWN) }
        )
        val issues = Expediter(Ignore.NOTHING, scanner, PlatformClassloaderTypeProvider).findIssues()

        expectThat(issues).containsExactlyInAnyOrder(
            Issue.MissingMember(
                "com/toasttab/expediter/test/caller/MissingMemberCaller",
                MemberAccess.MethodAccess(
                    "com/toasttab/expediter/test/MethodAccessChange",
                    null,
                    MemberSymbolicReference("voidToNonVoidReturnType", "(Ljava/lang/String;)V"),
                    MethodAccessType.VIRTUAL
                )
            ),

            Issue.MissingMember(
                "com/toasttab/expediter/test/caller/InheritanceCaller",
                MemberAccess.MethodAccess(
                    "com/toasttab/expediter/test/FinalizedSuperclass",
                    null,
                    MemberSymbolicReference("movedToGrandparent", "()V"),
                    MethodAccessType.SPECIAL
                )
            ),

            Issue.AccessInstanceMemberStatically(
                "com/toasttab/expediter/test/caller/StaticMismatchCaller",
                MemberAccess.MethodAccess(
                    "com/toasttab/expediter/test/MethodAccessChange",
                    "com/toasttab/expediter/test/MethodAccessChange",
                    MemberSymbolicReference("staticToInstance", "()V"),
                    MethodAccessType.STATIC
                )
            ),

            Issue.AccessInaccessibleMember(
                "com/toasttab/expediter/test/caller/AccessPermissionCaller",
                MemberAccess.MethodAccess(
                    "com/toasttab/expediter/test/MethodAccessChange",
                    "com/toasttab/expediter/test/MethodAccessChange",
                    MemberSymbolicReference("publicToPrivate", "(I)V"),
                    MethodAccessType.VIRTUAL
                )
            ),

            Issue.AccessInaccessibleMember(
                "com/toasttab/expediter/test/caller/AccessPermissionCaller",
                MemberAccess.MethodAccess(
                    "com/toasttab/expediter/test/MethodAccessChange",
                    "com/toasttab/expediter/test/MethodAccessChange",
                    MemberSymbolicReference("publicToPackagePrivate", "(J)V"),
                    MethodAccessType.VIRTUAL
                )
            ),

            Issue.AccessInaccessibleMember(
                "com/toasttab/expediter/test/caller/AccessPermissionCaller",
                MemberAccess.MethodAccess(
                    "com/toasttab/expediter/test/FieldAccessChange",
                    "com/toasttab/expediter/test/MethodAccessChange",
                    MemberSymbolicReference("publicToProtected", "(F)V"),
                    MethodAccessType.VIRTUAL
                )
            ),

            Issue.MissingMember(
                "com/toasttab/expediter/test/caller/MissingMemberCaller",
                MemberAccess.FieldAccess(
                    "com/toasttab/expediter/test/FieldAccessChange",
                    null,
                    MemberSymbolicReference("typeChangedField", "Ljava/lang/String;"),
                    FieldAccessType.INSTANCE
                )
            ),

            Issue.AccessInstanceMemberStatically(
                "com/toasttab/expediter/test/caller/StaticMismatchCaller",
                MemberAccess.FieldAccess(
                    "com/toasttab/expediter/test/FieldAccessChange",
                    "com/toasttab/expediter/test/FieldAccessChange",
                    MemberSymbolicReference("staticToInstanceField", "I"),
                    FieldAccessType.STATIC
                )
            ),

            Issue.AccessStaticMemberNonStatically(
                "com/toasttab/expediter/test/caller/StaticMismatchCaller",
                MemberAccess.FieldAccess(
                    "com/toasttab/expediter/test/FieldAccessChange",
                    "com/toasttab/expediter/test/FieldAccessChange",
                    MemberSymbolicReference("instanceToStaticField", "I"),
                    FieldAccessType.INSTANCE
                )
            ),

            Issue.AccessInaccessibleMember(
                "com/toasttab/expediter/test/caller/AccessPermissionCaller",
                MemberAccess.FieldAccess(
                    "com/toasttab/expediter/test/FieldAccessChange",
                    "com/toasttab/expediter/test/FieldAccessChange",
                    MemberSymbolicReference("publicToPrivateField", "I"),
                    FieldAccessType.INSTANCE
                )
            ),

            Issue.AccessInaccessibleMember(
                "com/toasttab/expediter/test/caller/AccessPermissionCaller",
                MemberAccess.FieldAccess(
                    "com/toasttab/expediter/test/MethodAccessChange",
                    "com/toasttab/expediter/test/MethodAccessChangeParent",
                    MemberSymbolicReference("publicToPackagePrivateField", "I"),
                    FieldAccessType.INSTANCE
                )
            ),

            Issue.VirtualCallToInterface(
                "com/toasttab/expediter/test/caller/TypeConversionCaller",
                MemberAccess.MethodAccess(
                    "com/toasttab/expediter/test/ClassToInterface",
                    "com/toasttab/expediter/test/ClassToInterface",
                    MemberSymbolicReference("formerlyClassMethod", "()V"),
                    MethodAccessType.VIRTUAL
                )
            ),

            Issue.InterfaceCallToClass(
                "com/toasttab/expediter/test/caller/TypeConversionCaller",
                MemberAccess.MethodAccess(
                    "com/toasttab/expediter/test/InterfaceToClass",
                    "com/toasttab/expediter/test/InterfaceToClass",
                    MemberSymbolicReference("formerlyInterfaceMethod", "()V"),
                    MethodAccessType.INTERFACE
                )
            ),

            Issue.MissingApplicationSuperType(
                "com/toasttab/expediter/test/ImplementsRemovedInterface",
                setOf("com/toasttab/expediter/test/RemovedSuperInterface")
            ),

            Issue.DuplicateType(
                "com/toasttab/expediter/test/DuplicateClass",
                listOf("testFixtures", "lib2-test-fixtures.jar")
            ),

            Issue.FinalApplicationSuperType(
                "com/toasttab/expediter/test/caller/InheritanceCaller",
                setOf("com/toasttab/expediter/test/FinalizedSuperclass")
            ),

            Issue.MissingType(
                "com/toasttab/expediter/test/caller/MissingTypeCaller",
                "com/toasttab/expediter/test/RemovedClass"
            ),

            Issue.MissingApplicationSuperType(
                "com/toasttab/expediter/test/caller/ExtendsRemovedClass",
                setOf("com/toasttab/expediter/test/RemovedClass")
            ),

            Issue.MissingType(
                "com/toasttab/expediter/test/caller/MissingTypeCaller",
                "com/toasttab/expediter/test/RemovedException"
            ),

            Issue.MissingType(
                "com/toasttab/expediter/test/caller/MissingTypeCaller",
                "com/toasttab/expediter/test/RemovedParamType"
            ),

            Issue.MissingType(
                "com/toasttab/expediter/test/caller/MissingTypeCaller",
                "com/toasttab/expediter/test/RemovedInstanceofTarget"
            ),

            Issue.MissingType(
                "com/toasttab/expediter/test/MethodAccessChange",
                "com/toasttab/expediter/test/RemovedParamType"
            ),

            Issue.MissingType(
                "com/toasttab/expediter/test/caller/MissingTypeCaller",
                "com/toasttab/expediter/test/RemovedFunctionalInterface"
            )
        )
    }
}
