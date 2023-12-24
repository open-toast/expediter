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
