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

package com.toasttab.expediter.sniffer

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream

class DeserializerTest {
    @Test
    fun `deserialize set`() {
        val buffer = ByteArrayOutputStream()

        ObjectOutputStream(buffer).use {
            it.writeObject(hashSetOf("a", "b", "c"))
            it.writeObject(linkedSetOf("x", "y"))
        }

        val d = Deserializer(buffer.toByteArray().inputStream())

        d.readStreamHeader()

        expectThat(d.readStringSet()).isEqualTo(setOf("a", "b", "c"))
        expectThat(d.readStringSet()).isEqualTo(setOf("x", "y"))
    }

    @Test
    fun `deserialize array`() {
        val buffer = ByteArrayOutputStream()

        ObjectOutputStream(buffer).use {
            it.writeObject(arrayOf("a", "b", "c"))
            it.writeObject(arrayOf("x", "y"))
        }

        val d = Deserializer(buffer.toByteArray().inputStream())

        d.readStreamHeader()

        expectThat(d.readStringArray()).isEqualTo(listOf("a", "b", "c"))
        expectThat(d.readStringArray()).isEqualTo(listOf("x", "y"))
    }
}
