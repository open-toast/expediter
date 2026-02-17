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
