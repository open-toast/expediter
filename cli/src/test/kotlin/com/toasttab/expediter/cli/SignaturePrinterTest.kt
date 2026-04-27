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

package com.toasttab.expediter.cli

import org.junit.jupiter.api.Test
import protokt.v1.toasttab.expediter.v1.AccessDeclaration
import protokt.v1.toasttab.expediter.v1.AccessProtection
import protokt.v1.toasttab.expediter.v1.MemberDescriptor
import protokt.v1.toasttab.expediter.v1.SymbolicReference
import protokt.v1.toasttab.expediter.v1.TypeDescriptor
import protokt.v1.toasttab.expediter.v1.TypeDescriptors
import protokt.v1.toasttab.expediter.v1.TypeExtensibility
import protokt.v1.toasttab.expediter.v1.TypeFlavor
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEqualTo
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class SignaturePrinterTest {
    private fun render(descriptors: TypeDescriptors): String {
        val out = ByteArrayOutputStream()
        SignaturePrinter.print(descriptors, PrintStream(out))
        return out.toString(Charsets.UTF_8)
    }

    private fun method(
        name: String,
        signature: String,
        protection: AccessProtection = AccessProtection.PUBLIC,
        declaration: AccessDeclaration = AccessDeclaration.INSTANCE
    ) = MemberDescriptor {
        this.ref = SymbolicReference {
            this.name = name
            this.signature = signature
        }
        this.protection = protection
        this.declaration = declaration
    }

    private fun field(
        name: String,
        signature: String,
        protection: AccessProtection = AccessProtection.PUBLIC,
        declaration: AccessDeclaration = AccessDeclaration.INSTANCE
    ) = method(name, signature, protection, declaration)

    @Test
    fun `interface with a super-interface uses extends`() {
        val types = TypeDescriptors {
            description = "demo"
            types = listOf(
                TypeDescriptor {
                    name = "com/example/Foo"
                    superName = "java/lang/Object"
                    interfaces = listOf("com/example/Bar")
                    flavor = TypeFlavor.INTERFACE
                    extensibility = TypeExtensibility.NOT_FINAL
                    protection = AccessProtection.PUBLIC
                    fields = emptyList()
                    methods = emptyList()
                }
            )
        }

        val output = render(types)

        expectThat(output).contains("# demo")
        expectThat(output).contains("# 1 type(s)")
        expectThat(output).contains("public interface com.example.Foo extends com.example.Bar {")
    }

    @Test
    fun `class that implements interfaces uses implements`() {
        val types = TypeDescriptors {
            types = listOf(
                TypeDescriptor {
                    name = "com/example/Foo"
                    superName = "com/example/Base"
                    interfaces = listOf("com/example/A", "com/example/B")
                    flavor = TypeFlavor.CLASS
                    extensibility = TypeExtensibility.NOT_FINAL
                    protection = AccessProtection.PUBLIC
                    fields = emptyList()
                    methods = emptyList()
                }
            )
        }

        expectThat(render(types)).contains(
            "public class com.example.Foo extends com.example.Base implements com.example.A, com.example.B {"
        )
    }

    @Test
    fun `unknown flavor falls back to type`() {
        val types = TypeDescriptors {
            types = listOf(
                TypeDescriptor {
                    name = "com/example/Foo"
                    superName = "java/lang/Object"
                    interfaces = emptyList()
                    flavor = TypeFlavor.UNKNOWN
                    extensibility = TypeExtensibility.FINAL
                    protection = AccessProtection.PACKAGE_PRIVATE
                    fields = emptyList()
                    methods = emptyList()
                }
            )
        }

        expectThat(render(types)).contains("final type com.example.Foo {")
    }

    @Test
    fun `members render with modifiers and translated primitive and array types`() {
        val types = TypeDescriptors {
            types = listOf(
                TypeDescriptor {
                    name = "com/example/Foo"
                    superName = "java/lang/Object"
                    interfaces = emptyList()
                    flavor = TypeFlavor.CLASS
                    extensibility = TypeExtensibility.FINAL
                    protection = AccessProtection.PUBLIC
                    fields = listOf(
                        field("COUNT", "I", AccessProtection.PUBLIC, AccessDeclaration.STATIC),
                        field("names", "[Ljava/lang/String;", AccessProtection.PROTECTED)
                    )
                    methods = listOf(
                        method(
                            "compute",
                            "(JD[Z)V",
                            AccessProtection.PRIVATE,
                            AccessDeclaration.INSTANCE
                        )
                    )
                }
            )
        }

        val output = render(types)

        expectThat(output).contains("public final class com.example.Foo {")
        expectThat(output).contains("    public static int COUNT")
        expectThat(output).contains("    protected java.lang.String[] names")
        expectThat(output).contains("    private void compute(long, double, boolean[])")
    }

    @Test
    fun `every primitive descriptor renders to its java name`() {
        val types = TypeDescriptors {
            types = listOf(
                TypeDescriptor {
                    name = "com/example/Foo"
                    superName = "java/lang/Object"
                    interfaces = emptyList()
                    flavor = TypeFlavor.CLASS
                    extensibility = TypeExtensibility.NOT_FINAL
                    protection = AccessProtection.PUBLIC
                    fields = emptyList()
                    methods = listOf(
                        method("m", "(ZBCSIJFD)V")
                    )
                }
            )
        }

        expectThat(render(types)).contains(
            "    public void m(boolean, byte, char, short, int, long, float, double)"
        )
    }

    @Test
    fun `final interface does not double up the final keyword`() {
        val types = TypeDescriptors {
            types = listOf(
                TypeDescriptor {
                    name = "com/example/Foo"
                    superName = "java/lang/Object"
                    interfaces = emptyList()
                    flavor = TypeFlavor.INTERFACE
                    extensibility = TypeExtensibility.FINAL
                    protection = AccessProtection.PUBLIC
                    fields = emptyList()
                    methods = emptyList()
                }
            )
        }

        expectThat(render(types)).contains("public interface com.example.Foo {")
    }

    @Test
    fun `package-private members render without a protection keyword`() {
        val types = TypeDescriptors {
            types = listOf(
                TypeDescriptor {
                    name = "com/example/Foo"
                    superName = "java/lang/Object"
                    interfaces = emptyList()
                    flavor = TypeFlavor.CLASS
                    extensibility = TypeExtensibility.NOT_FINAL
                    protection = AccessProtection.PUBLIC
                    fields = listOf(
                        field("hidden", "I", AccessProtection.PACKAGE_PRIVATE)
                    )
                    methods = emptyList()
                }
            )
        }

        expectThat(render(types)).contains("    int hidden")
    }

    @Test
    fun `empty description omits header line`() {
        val types = TypeDescriptors {
            types = listOf(
                TypeDescriptor {
                    name = "com/example/Foo"
                    superName = "java/lang/Object"
                    interfaces = emptyList()
                    flavor = TypeFlavor.CLASS
                    extensibility = TypeExtensibility.NOT_FINAL
                    protection = AccessProtection.PUBLIC
                    fields = emptyList()
                    methods = emptyList()
                }
            )
        }

        val lines = render(types).lines()

        expectThat(lines[0]).isEqualTo("# 1 type(s)")
    }
}
