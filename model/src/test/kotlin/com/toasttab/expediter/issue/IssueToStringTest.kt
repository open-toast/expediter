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
    fun `DuplicateType`() {
        expectThat(Issue.DuplicateType("com/foo/Bar", listOf("a.jar", "b.jar")).toString())
            .isEqualTo("duplicate class com/foo/Bar in [a.jar, b.jar]")
    }

    @Test
    fun `MissingType`() {
        expectThat(Issue.MissingType("com/foo/Caller", "com/foo/Missing").toString())
            .isEqualTo("com/foo/Caller refers to missing type com/foo/Missing")
    }

    @Test
    fun `MissingApplicationSuperType singular`() {
        expectThat(Issue.MissingApplicationSuperType("com/foo/Caller", setOf("com/foo/Base")).toString())
            .isEqualTo("com/foo/Caller extends missing type com/foo/Base")
    }

    @Test
    fun `MissingApplicationSuperType plural`() {
        expectThat(Issue.MissingApplicationSuperType("com/foo/Caller", setOf("com/foo/Base", "com/foo/Iface")).toString())
            .isEqualTo("com/foo/Caller extends missing types com/foo/Base, com/foo/Iface")
    }

    @Test
    fun `FinalApplicationSuperType singular`() {
        expectThat(Issue.FinalApplicationSuperType("com/foo/Caller", setOf("com/foo/Final")).toString())
            .isEqualTo("com/foo/Caller extends final type com/foo/Final")
    }

    @Test
    fun `FinalApplicationSuperType plural`() {
        expectThat(Issue.FinalApplicationSuperType("com/foo/Caller", setOf("com/foo/Final", "com/foo/Sealed")).toString())
            .isEqualTo("com/foo/Caller extends final types com/foo/Final, com/foo/Sealed")
    }

    @Test
    fun `MissingSuperType singular`() {
        expectThat(Issue.MissingSuperType("com/foo/Caller", "com/foo/Target", setOf("com/foo/Super")).toString())
            .isEqualTo("com/foo/Caller refers to type com/foo/Target with missing supertype com/foo/Super")
    }

    @Test
    fun `MissingSuperType plural`() {
        expectThat(Issue.MissingSuperType("com/foo/Caller", "com/foo/Target", setOf("com/foo/Super1", "com/foo/Super2")).toString())
            .isEqualTo("com/foo/Caller refers to type com/foo/Target with missing supertypes com/foo/Super1, com/foo/Super2")
    }

    @Test
    fun `MissingMember`() {
        expectThat(
            Issue.MissingMember(
                "com/foo/Caller",
                MemberAccess.MethodAccess("com/foo/Bar", null, MemberSymbolicReference("bar", "()V"), MethodAccessType.VIRTUAL)
            ).toString()
        ).isEqualTo("com/foo/Caller accesses missing com/foo/Bar.bar()V")
    }

    @Test
    fun `AccessStaticMemberNonStatically`() {
        expectThat(
            Issue.AccessStaticMemberNonStatically(
                "com/foo/Caller",
                MemberAccess.FieldAccess("com/foo/Bar", "com/foo/Bar", MemberSymbolicReference("x", "I"), FieldAccessType.INSTANCE)
            ).toString()
        ).isEqualTo("com/foo/Caller accesses static com/foo/Bar.xI non-statically")
    }

    @Test
    fun `AccessInstanceMemberStatically`() {
        expectThat(
            Issue.AccessInstanceMemberStatically(
                "com/foo/Caller",
                MemberAccess.MethodAccess("com/foo/Bar", "com/foo/Bar", MemberSymbolicReference("bar", "()V"), MethodAccessType.STATIC)
            ).toString()
        ).isEqualTo("com/foo/Caller accesses instance com/foo/Bar.bar()V statically")
    }

    @Test
    fun `AccessInaccessibleMember`() {
        expectThat(
            Issue.AccessInaccessibleMember(
                "com/foo/Caller",
                MemberAccess.MethodAccess("com/foo/Bar", "com/foo/Bar", MemberSymbolicReference("bar", "(I)V"), MethodAccessType.VIRTUAL)
            ).toString()
        ).isEqualTo("com/foo/Caller accesses inaccessible com/foo/Bar.bar(I)V")
    }

    @Test
    fun `AccessInaccessibleMember via different type`() {
        expectThat(
            Issue.AccessInaccessibleMember(
                "com/foo/Caller",
                MemberAccess.MethodAccess("com/foo/Baz", "com/foo/Bar", MemberSymbolicReference("bar", "(F)V"), MethodAccessType.VIRTUAL)
            ).toString()
        ).isEqualTo("com/foo/Caller accesses inaccessible com/foo/Bar.bar(F)V (via com/foo/Baz)")
    }

    @Test
    fun `VirtualCallToInterface`() {
        expectThat(
            Issue.VirtualCallToInterface(
                "com/foo/Caller",
                MemberAccess.MethodAccess("com/foo/Iface", "com/foo/Iface", MemberSymbolicReference("foo", "()V"), MethodAccessType.VIRTUAL)
            ).toString()
        ).isEqualTo("com/foo/Caller accesses interface method com/foo/Iface.foo()V virtually")
    }

    @Test
    fun `InterfaceCallToClass`() {
        expectThat(
            Issue.InterfaceCallToClass(
                "com/foo/Caller",
                MemberAccess.MethodAccess("com/foo/Clazz", "com/foo/Clazz", MemberSymbolicReference("foo", "()V"), MethodAccessType.INTERFACE)
            ).toString()
        ).isEqualTo("com/foo/Caller accesses class method com/foo/Clazz.foo()V interfacely")
    }

    @Test
    fun `SpecialCallOutOfHierarchy`() {
        expectThat(
            Issue.SpecialCallOutOfHierarchy(
                "com/foo/Caller",
                MemberAccess.MethodAccess("com/foo/Other", "com/foo/Other", MemberSymbolicReference("foo", "()V"), MethodAccessType.SPECIAL)
            ).toString()
        ).isEqualTo("com/foo/Caller makes a special call to non-contructor com/foo/Other.foo()V which is not in its hierarchy")
    }
}
