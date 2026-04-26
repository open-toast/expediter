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

import com.toasttab.expediter.parser.SignatureParser
import com.toasttab.expediter.parser.TypeSignature
import protokt.v1.toasttab.expediter.v1.AccessDeclaration
import protokt.v1.toasttab.expediter.v1.AccessProtection
import protokt.v1.toasttab.expediter.v1.MemberDescriptor
import protokt.v1.toasttab.expediter.v1.TypeDescriptor
import protokt.v1.toasttab.expediter.v1.TypeDescriptors
import protokt.v1.toasttab.expediter.v1.TypeExtensibility
import protokt.v1.toasttab.expediter.v1.TypeFlavor
import java.io.PrintStream

object SignaturePrinter {
    fun print(descriptors: TypeDescriptors, out: PrintStream) {
        if (descriptors.description.isNotEmpty()) {
            out.println("# ${descriptors.description}")
            out.println()
        }
        out.println("# ${descriptors.types.size} type(s)")
        out.println()

        for (type in descriptors.types.sortedBy { it.name }) {
            printType(type, out)
        }
    }

    private fun printType(type: TypeDescriptor, out: PrintStream) {
        val keyword = when (type.flavor) {
            TypeFlavor.INTERFACE -> "interface"
            TypeFlavor.CLASS -> "class"
            else -> "type"
        }

        out.append(renderAccess(type.protection))
        if (type.extensibility == TypeExtensibility.FINAL && type.flavor != TypeFlavor.INTERFACE) {
            out.append("final ")
        }
        out.append(keyword).append(' ').append(renderClassName(type.name))

        type.superName?.takeUnless { it == "java/lang/Object" }?.let {
            out.append(" extends ").append(renderClassName(it))
        }
        if (type.interfaces.isNotEmpty()) {
            val keyword2 = if (type.flavor == TypeFlavor.INTERFACE) "extends" else "implements"
            out.append(' ').append(keyword2).append(' ')
            out.append(type.interfaces.joinToString(", ", transform = ::renderClassName))
        }
        out.println(" {")

        val members = (type.fields.map { it to MemberKind.FIELD } + type.methods.map { it to MemberKind.METHOD })
            .sortedWith(
                compareBy(
                    { it.second },
                    { it.first.requireRef.name },
                    { it.first.requireRef.signature }
                )
            )

        for ((member, kind) in members) {
            out.append("    ")
            renderMember(member, kind, out)
            out.println()
        }

        out.println("}")
        out.println()
    }

    private enum class MemberKind { FIELD, METHOD }

    private fun renderMember(member: MemberDescriptor, kind: MemberKind, out: PrintStream) {
        out.append(renderAccess(member.protection))
        if (member.declaration == AccessDeclaration.STATIC) {
            out.append("static ")
        }

        val ref = member.requireRef
        when (kind) {
            MemberKind.FIELD -> {
                val type = SignatureParser.parseType(ref.signature)
                out.append(renderType(type)).append(' ').append(ref.name)
            }

            MemberKind.METHOD -> {
                val sig = SignatureParser.parseMethod(ref.signature)
                out.append(renderType(sig.returnType)).append(' ').append(ref.name).append('(')
                out.append(sig.argumentTypes.joinToString(", ", transform = ::renderType))
                out.append(')')
            }
        }
    }

    private fun renderAccess(protection: AccessProtection): String = when (protection) {
        AccessProtection.PUBLIC -> "public "
        AccessProtection.PROTECTED -> "protected "
        AccessProtection.PRIVATE -> "private "
        AccessProtection.PACKAGE_PRIVATE -> ""
        else -> ""
    }

    private fun renderType(type: TypeSignature): String {
        val scalar = if (type.primitive) primitiveName(type.scalarName) else renderClassName(type.scalarName)
        return scalar + "[]".repeat(type.dimensions)
    }

    private fun renderClassName(internal: String): String = internal.replace('/', '.')

    private fun primitiveName(descriptor: String): String = when (descriptor) {
        "V" -> "void"
        "Z" -> "boolean"
        "B" -> "byte"
        "C" -> "char"
        "S" -> "short"
        "I" -> "int"
        "J" -> "long"
        "F" -> "float"
        "D" -> "double"
        else -> descriptor
    }
}
