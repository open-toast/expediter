package com.toasttab.expediter.ignore

import com.toasttab.expediter.issue.Issue
import com.toasttab.expediter.types.MemberAccess
import com.toasttab.expediter.types.MemberSymbolicReference
import com.toasttab.expediter.types.MethodAccessType
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isFalse
import strikt.assertions.isTrue

class IgnoreTest {
    @Test
    fun `caller starts with exact`() {
        expectThat(
            Ignore.CallerStartsWith("java/lang/String", "com/foo").ignore(
                Issue.MissingType("java/lang/String", "foo/Zz")
            )
        ).isTrue()
    }

    @Test
    fun `caller starts with first`() {
        expectThat(
            Ignore.CallerStartsWith("java/lang", "zzz/foo").ignore(
                Issue.MissingType("java/lang/String", "foo/Zz")
            )
        ).isTrue()
    }

    @Test
    fun `caller starts with last`() {
        expectThat(
            Ignore.CallerStartsWith("java/lang", "aaa/foo").ignore(
                Issue.MissingType("java/lang/String", "foo/Zz")
            )
        ).isTrue()
    }

    @Test
    fun `caller starts with middle`() {
        expectThat(
            Ignore.CallerStartsWith("java/lang", "aaa/foo", "zzz/foo").ignore(
                Issue.MissingType("java/lang/String", "foo/Zz")
            )
        ).isTrue()
    }

    @Test
    fun `caller starts negative first`() {
        expectThat(
            Ignore.CallerStartsWith("zzz/foo").ignore(
                Issue.MissingType("java/lang/String", "foo/Zz")
            )
        ).isFalse()
    }

    @Test
    fun `caller starts negative last`() {
        expectThat(
            Ignore.CallerStartsWith("aaa/foo").ignore(
                Issue.MissingType("java/lang/String", "foo/Zz")
            )
        ).isFalse()
    }

    @Test
    fun `caller starts negative middle`() {
        expectThat(
            Ignore.CallerStartsWith("aaa/foo", "zzz/foo", "java/lang/StringX").ignore(
                Issue.MissingType("java/lang/String", "foo/Zz")
            )
        ).isFalse()
    }

    @Test
    fun `target starts with exact`() {
        expectThat(
            Ignore.TargetStartsWith("java/lang/String", "com/foo").ignore(
                Issue.MissingType("foo/Zz", "java/lang/String")
            )
        ).isTrue()
    }

    @Test
    fun `target starts with first`() {
        expectThat(
            Ignore.TargetStartsWith("java/lang", "zzz/foo").ignore(
                Issue.MissingType("foo/Zz", "java/lang/String")
            )
        ).isTrue()
    }

    @Test
    fun `target starts with last`() {
        expectThat(
            Ignore.TargetStartsWith("java/lang", "aaa/foo").ignore(
                Issue.MissingType("foo/Zz", "java/lang/String")
            )
        ).isTrue()
    }

    @Test
    fun `target starts with middle`() {
        expectThat(
            Ignore.TargetStartsWith("java/lang", "aaa/foo", "zzz/foo").ignore(
                Issue.MissingType("foo/Zz", "java/lang/String")
            )
        ).isTrue()
    }

    @Test
    fun `target starts negative first`() {
        expectThat(
            Ignore.TargetStartsWith("zzz/foo").ignore(
                Issue.MissingType("foo/Zz", "java/lang/String")
            )
        ).isFalse()
    }

    @Test
    fun `target starts negative last`() {
        expectThat(
            Ignore.TargetStartsWith("aaa/foo").ignore(
                Issue.MissingType("foo/Zz", "java/lang/String")
            )
        ).isFalse()
    }

    @Test
    fun `target starts negative middle`() {
        expectThat(
            Ignore.TargetStartsWith("aaa/foo", "zzz/foo", "java/lang/StringX").ignore(
                Issue.MissingType("foo/Zz", "java/lang/String")
            )
        ).isFalse()
    }

    @Test
    fun `caller starts with null`() {
        expectThat(
            Ignore.CallerStartsWith("com/Foo").ignore(
                Issue.DuplicateType("com/Foo", listOf("lib1.jar", "lib2.jar"))
            )
        ).isFalse()
    }

    @Test
    fun `default constructor`() {
        expectThat(
            Ignore.And(
                Ignore.IsConstructor,
                Ignore.Signature.IS_BLANK
            ).ignore(
                Issue.MissingMember(
                    "com/Foo",
                    MemberAccess.MethodAccess(
                        "java/lang/String",
                        null,
                        MemberSymbolicReference("<init>", "()V"),
                        MethodAccessType.SPECIAL
                    )
                )
            )
        ).isTrue()
    }
}
