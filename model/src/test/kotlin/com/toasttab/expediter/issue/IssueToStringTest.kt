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

package com.toasttab.expediter.issue

import com.toasttab.expediter.types.FieldAccessType
import com.toasttab.expediter.types.MemberAccess
import com.toasttab.expediter.types.MemberSymbolicReference
import com.toasttab.expediter.types.MethodAccessType
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class IssueToStringTest {
    @Test
    fun `DuplicateType toString`() {
        expectThat(
            Issue.DuplicateType("com/example/Foo", listOf("source1", "source2")).toString()
        ).isEqualTo("duplicate class com/example/Foo in [source1, source2]")
    }

    @Test
    fun `MissingType toString`() {
        expectThat(
            Issue.MissingType("com/example/Caller", "com/example/Missing").toString()
        ).isEqualTo("com/example/Caller refers to missing type com/example/Missing")
    }

    @Test
    fun `MissingApplicationSuperType toString with single missing type`() {
        expectThat(
            Issue.MissingApplicationSuperType("com/example/Caller", setOf("com/example/Super")).toString()
        ).isEqualTo("com/example/Caller extends missing type com/example/Super")
    }

    @Test
    fun `MissingApplicationSuperType toString with multiple missing types`() {
        expectThat(
            Issue.MissingApplicationSuperType("com/example/Caller", setOf("com/example/A", "com/example/B")).toString()
        ).isEqualTo("com/example/Caller extends missing types com/example/A, com/example/B")
    }

    @Test
    fun `FinalApplicationSuperType toString with single final type`() {
        expectThat(
            Issue.FinalApplicationSuperType("com/example/Caller", setOf("com/example/Final")).toString()
        ).isEqualTo("com/example/Caller extends final type com/example/Final")
    }

    @Test
    fun `FinalApplicationSuperType toString with multiple final types`() {
        expectThat(
            Issue.FinalApplicationSuperType("com/example/Caller", setOf("com/example/A", "com/example/B")).toString()
        ).isEqualTo("com/example/Caller extends final types com/example/A, com/example/B")
    }

    @Test
    fun `MissingSuperType toString with single missing supertype`() {
        expectThat(
            Issue.MissingSuperType("com/example/Caller", "com/example/Target", setOf("com/example/Super")).toString()
        ).isEqualTo("com/example/Caller refers to type com/example/Target with missing supertype com/example/Super")
    }

    @Test
    fun `MissingSuperType toString with multiple missing supertypes`() {
        expectThat(
            Issue.MissingSuperType("com/example/Caller", "com/example/Target", setOf("com/example/A", "com/example/B")).toString()
        ).isEqualTo("com/example/Caller refers to type com/example/Target with missing supertypes com/example/A, com/example/B")
    }

    @Test
    fun `MissingMember toString with method`() {
        expectThat(
            Issue.MissingMember(
                "com/example/Caller",
                MemberAccess.MethodAccess("com/example/Target", null, MemberSymbolicReference("foo", "()V"), MethodAccessType.VIRTUAL)
            ).toString()
        ).isEqualTo("com/example/Caller accesses missing com/example/Target.foo()V")
    }

    @Test
    fun `MissingMember toString with field`() {
        expectThat(
            Issue.MissingMember(
                "com/example/Caller",
                MemberAccess.FieldAccess("com/example/Target", null, MemberSymbolicReference("bar", "I"), FieldAccessType.INSTANCE)
            ).toString()
        ).isEqualTo("com/example/Caller accesses missing com/example/Target.barI")
    }

    @Test
    fun `AccessStaticMemberNonStatically toString`() {
        expectThat(
            Issue.AccessStaticMemberNonStatically(
                "com/example/Caller",
                MemberAccess.FieldAccess("com/example/Target", "com/example/Target", MemberSymbolicReference("field", "I"), FieldAccessType.INSTANCE)
            ).toString()
        ).isEqualTo("com/example/Caller accesses static com/example/Target.fieldI non-statically")
    }

    @Test
    fun `AccessInstanceMemberStatically toString`() {
        expectThat(
            Issue.AccessInstanceMemberStatically(
                "com/example/Caller",
                MemberAccess.MethodAccess("com/example/Target", "com/example/Target", MemberSymbolicReference("foo", "()V"), MethodAccessType.STATIC)
            ).toString()
        ).isEqualTo("com/example/Caller accesses instance com/example/Target.foo()V statically")
    }

    @Test
    fun `AccessInaccessibleMember toString`() {
        expectThat(
            Issue.AccessInaccessibleMember(
                "com/example/Caller",
                MemberAccess.MethodAccess("com/example/Target", "com/example/Target", MemberSymbolicReference("secret", "(I)V"), MethodAccessType.VIRTUAL)
            ).toString()
        ).isEqualTo("com/example/Caller accesses inaccessible com/example/Target.secret(I)V")
    }

    @Test
    fun `AccessInaccessibleMember toString with different declaring type`() {
        expectThat(
            Issue.AccessInaccessibleMember(
                "com/example/Caller",
                MemberAccess.MethodAccess("com/example/Target", "com/example/Declaring", MemberSymbolicReference("secret", "(I)V"), MethodAccessType.VIRTUAL)
            ).toString()
        ).isEqualTo("com/example/Caller accesses inaccessible com/example/Declaring.secret(I)V (via com/example/Target)")
    }

    @Test
    fun `VirtualCallToInterface toString`() {
        expectThat(
            Issue.VirtualCallToInterface(
                "com/example/Caller",
                MemberAccess.MethodAccess("com/example/Target", "com/example/Target", MemberSymbolicReference("foo", "()V"), MethodAccessType.VIRTUAL)
            ).toString()
        ).isEqualTo("com/example/Caller accesses interface method com/example/Target.foo()V virtually")
    }

    @Test
    fun `InterfaceCallToClass toString`() {
        expectThat(
            Issue.InterfaceCallToClass(
                "com/example/Caller",
                MemberAccess.MethodAccess("com/example/Target", "com/example/Target", MemberSymbolicReference("foo", "()V"), MethodAccessType.INTERFACE)
            ).toString()
        ).isEqualTo("com/example/Caller accesses class method com/example/Target.foo()V interfacely")
    }

    @Test
    fun `SpecialCallOutOfHierarchy toString`() {
        expectThat(
            Issue.SpecialCallOutOfHierarchy(
                "com/example/Caller",
                MemberAccess.MethodAccess("com/example/Target", "com/example/Target", MemberSymbolicReference("foo", "()V"), MethodAccessType.SPECIAL)
            ).toString()
        ).isEqualTo("com/example/Caller makes a special call to non-contructor com/example/Target.foo()V which is not in its hierarchy")
    }
}
