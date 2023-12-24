package com.toasttab.expediter.ignore

import com.toasttab.expediter.issue.Issue
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
}
