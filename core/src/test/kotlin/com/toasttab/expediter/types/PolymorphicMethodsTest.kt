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

package com.toasttab.expediter.types

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isTrue
import java.lang.invoke.MethodHandle
import java.lang.invoke.VarHandle
import java.lang.reflect.Modifier

class PolymorphicMethodsTest {
    @Test
    fun `MethodHandle and VarHandle methods annotated with PolymorphicSignature`() {
        for (cls in listOf(MethodHandle::class.java, VarHandle::class.java)) {
            val name = cls.name.replace('.', '/')

            for (method in polymorphicMethodsOf(cls)) {
                expectThat(PolymorphicMethods.contains(name, method.name)).isTrue()
            }
        }
    }

    private fun polymorphicMethodsOf(cls: Class<*>) = cls.methods.filter { m ->
        Modifier.isPublic(m.modifiers) && m.annotations.any {
            it.annotationClass.java.name == "java.lang.invoke.MethodHandle\$PolymorphicSignature"
        }
    }
}
