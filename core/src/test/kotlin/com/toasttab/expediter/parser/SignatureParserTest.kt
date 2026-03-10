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

package com.toasttab.expediter.parser

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class SignatureParserTest {
    @Test
    fun `primitive type round-trips`() {
        expectThat(SignatureParser.parseType("I").toDescriptor()).isEqualTo("I")
    }

    @Test
    fun `void type round-trips`() {
        expectThat(SignatureParser.parseType("V").toDescriptor()).isEqualTo("V")
    }

    @Test
    fun `object type round-trips`() {
        expectThat(SignatureParser.parseType("Ljava/lang/Object;").toDescriptor()).isEqualTo("Ljava/lang/Object;")
    }

    @Test
    fun `primitive array type round-trips`() {
        expectThat(SignatureParser.parseType("[I").toDescriptor()).isEqualTo("[I")
    }

    @Test
    fun `object array type round-trips`() {
        expectThat(SignatureParser.parseType("[Ljava/lang/String;").toDescriptor()).isEqualTo("[Ljava/lang/String;")
    }

    @Test
    fun `multi-dimensional array type round-trips`() {
        expectThat(SignatureParser.parseType("[[D").toDescriptor()).isEqualTo("[[D")
    }

    @Test
    fun `method with no args and void return round-trips`() {
        expectThat(SignatureParser.parseMethod("()V").toDescriptor()).isEqualTo("()V")
    }

    @Test
    fun `method with object arg and void return round-trips`() {
        val descriptor = "(Ljava/lang/Object;)V"
        expectThat(SignatureParser.parseMethod(descriptor).toDescriptor()).isEqualTo(descriptor)
    }

    @Test
    fun `method with multiple args round-trips`() {
        val descriptor = "(Ljava/lang/String;I[DLjava/util/List;)Ljava/lang/Object;"
        expectThat(SignatureParser.parseMethod(descriptor).toDescriptor()).isEqualTo(descriptor)
    }

    @Test
    fun `method with primitive args and return round-trips`() {
        val descriptor = "(IJ)Z"
        expectThat(SignatureParser.parseMethod(descriptor).toDescriptor()).isEqualTo(descriptor)
    }

    @Test
    fun `method with array args round-trips`() {
        val descriptor = "([Ljava/lang/String;)[Ljava/lang/Object;"
        expectThat(SignatureParser.parseMethod(descriptor).toDescriptor()).isEqualTo(descriptor)
    }

    @Test
    fun `TypeSignature toDescriptor for primitive`() {
        val sig = TypeSignature("Z", 0, true)
        expectThat(sig.toDescriptor()).isEqualTo("Z")
    }

    @Test
    fun `TypeSignature toDescriptor for object`() {
        val sig = TypeSignature("java/lang/String", 0, false)
        expectThat(sig.toDescriptor()).isEqualTo("Ljava/lang/String;")
    }

    @Test
    fun `TypeSignature toDescriptor for object array`() {
        val sig = TypeSignature("java/lang/String", 2, false)
        expectThat(sig.toDescriptor()).isEqualTo("[[Ljava/lang/String;")
    }

    @Test
    fun `TypeSignature toDescriptor for primitive array`() {
        val sig = TypeSignature("B", 1, true)
        expectThat(sig.toDescriptor()).isEqualTo("[B")
    }

    @Test
    fun `MethodSignature toDescriptor`() {
        val method = MethodSignature(
            returnType = TypeSignature("V", 0, true),
            argumentTypes = listOf(
                TypeSignature("java/lang/Object", 0, false),
                TypeSignature("I", 0, true)
            )
        )
        expectThat(method.toDescriptor()).isEqualTo("(Ljava/lang/Object;I)V")
    }
}
