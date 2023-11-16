package com.toasttab.expediter.issue

import com.toasttab.expediter.types.MemberAccess
import com.toasttab.expediter.types.MemberSymbolicReference
import com.toasttab.expediter.types.MethodAccessType
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo

class SerializationTest {
    @Test
    fun `can deserialize known and unknown issues`() {
        val report = Thread.currentThread().contextClassLoader.getResourceAsStream("issues.json").use {
            IssueReport.fromJson(it)
        }

        expectThat(report) {
            get { name }.isEqualTo("test")

            get { issues }.containsExactly(
                Issue.UnknownIssue("test-issue", "foo/Foo", "bar/Bar"),
                Issue.MissingMember(
                    "foo/Foo",
                    MemberAccess.MethodAccess(
                        "bar/Bar",
                        null,
                        MemberSymbolicReference("bar", "()V"),
                        MethodAccessType.VIRTUAL
                    )
                )
            )
        }
    }
}
