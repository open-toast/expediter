package com.toasttab.expediter.types

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isNotNull
import strikt.assertions.isNull

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
class JvmTypeProviderTest {
    @Test
    fun `java 8 has no Files#readString`() {
        val type = JvmTypeProvider.forTarget(8).lookupPlatformType("java/nio/file/Files")

        expectThat(type).isNotNull().and {
            get {
                members.find {
                    it.ref == MemberSymbolicReference.MethodSymbolicReference(
                        "readString",
                        "(Ljava/nio/file/Path;)Ljava/lang/String;"
                    )
                }
            }.isNull()
        }
    }

    @Test
    fun `java 11 has Files#readString`() {
        val type = JvmTypeProvider.forTarget(11).lookupPlatformType("java/nio/file/Files")

        expectThat(type).isNotNull().and {
            get {
                members.find {
                    it.ref == MemberSymbolicReference.MethodSymbolicReference(
                        "readString",
                        "(Ljava/nio/file/Path;)Ljava/lang/String;"
                    )
                }
            }.isNotNull()
        }

    }

    @Test
    fun `java 9 has no List#copyOf`() {
        val type = JvmTypeProvider.forTarget(9).lookupPlatformType("java/util/List")

        expectThat(type).isNotNull().and {
            get {
                members.find {
                    it.ref == MemberSymbolicReference.MethodSymbolicReference(
                        "copyOf",
                        "(Ljava/util/Collection;)Ljava/util/List;"
                    )
                }
            }.isNull()
        }
    }

    @Test
    fun `java 10 has List#copyOf`() {
        val type = JvmTypeProvider.forTarget(10).lookupPlatformType("java/util/List")

        expectThat(type).isNotNull().and {
            get {
                members.find {
                    it.ref == MemberSymbolicReference.MethodSymbolicReference(
                        "copyOf",
                        "(Ljava/util/Collection;)Ljava/util/List;"
                    )
                }
            }.isNotNull()
        }
    }

    @Test
    fun `java 10 has no HttpClient`() {
        val type = JvmTypeProvider.forTarget(10).lookupPlatformType("java/net/http/HttpClient")

        expectThat(type).isNull()
    }

    @Test
    fun `java 11 has HttpClient`() {
        val type = JvmTypeProvider.forTarget(11).lookupPlatformType("java/net/http/HttpClient")

        expectThat(type).isNotNull()
    }
}