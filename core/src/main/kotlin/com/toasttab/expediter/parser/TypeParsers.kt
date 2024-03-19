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

package com.toasttab.expediter.parser

import com.toasttab.model.types.ApplicationType
import com.toasttab.model.types.ClassfileSource
import com.toasttab.model.types.FieldAccessType
import com.toasttab.model.types.MemberAccess
import com.toasttab.model.types.MemberSymbolicReference
import com.toasttab.model.types.MethodAccessType
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassReader.SKIP_DEBUG
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.Handle
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Opcodes.ASM9
import protokt.v1.toasttab.expediter.v1.MemberDescriptor
import protokt.v1.toasttab.expediter.v1.SymbolicReference
import protokt.v1.toasttab.expediter.v1.TypeDescriptor
import java.io.InputStream

object TypeParsers {
    fun applicationType(stream: InputStream, source: ClassfileSource) = ApplicationTypeParser(source).apply {
        ClassReader(stream).accept(this, SKIP_DEBUG)
    }.get()

    fun typeDescriptor(stream: InputStream) = TypeDescriptorParser().apply {
        ClassReader(stream).accept(this, SKIP_DEBUG)
    }.get()
}

private class ApplicationTypeParser(private val source: ClassfileSource) : ClassVisitor(ASM9, TypeDescriptorParser()) {
    private val refs: MutableSet<MemberAccess<*>> = hashSetOf()
    private val referencedTypes: MutableSet<String> = hashSetOf()

    fun get() = ApplicationType((cv as TypeDescriptorParser).get(), refs, referencedTypes, source)

    override fun visitField(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        value: Any?
    ): FieldVisitor? {
        referencedTypes.addAll(SignatureParser.parseType(descriptor).referencedTypes())

        return super.visitField(access, name, descriptor, signature, value)
    }

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        super.visitMethod(access, name, descriptor, signature, exceptions)

        referencedTypes.addAll(SignatureParser.parseMethod(descriptor).referencedTypes())

        return object : MethodVisitor(ASM9) {
            override fun visitMethodInsn(
                opcode: Int,
                owner: String,
                name: String,
                descriptor: String,
                isInterface: Boolean
            ) {
                val invokeType = when (opcode) {
                    Opcodes.INVOKEVIRTUAL -> MethodAccessType.VIRTUAL
                    Opcodes.INVOKESPECIAL -> MethodAccessType.SPECIAL
                    Opcodes.INVOKEINTERFACE -> MethodAccessType.INTERFACE
                    Opcodes.INVOKESTATIC -> MethodAccessType.STATIC
                    else -> MethodAccessType.OTHER // e.g. indy
                }

                refs.add(
                    MemberAccess.MethodAccess(
                        owner,
                        null,
                        MemberSymbolicReference(name, descriptor),
                        invokeType
                    )
                )

                referencedTypes.addAll(SignatureParser.parseMethod(descriptor).referencedTypes())
                referencedTypes.add(owner)
            }

            override fun visitFieldInsn(opcode: Int, owner: String, name: String, descriptor: String) {
                val type = if (opcode == Opcodes.GETSTATIC || opcode == Opcodes.PUTSTATIC) {
                    FieldAccessType.STATIC
                } else {
                    FieldAccessType.INSTANCE
                }

                refs.add(
                    MemberAccess.FieldAccess(
                        owner,
                        null,
                        MemberSymbolicReference(name, descriptor),
                        type
                    )
                )

                referencedTypes.addAll(SignatureParser.parseType(descriptor).referencedTypes())
                referencedTypes.add(owner)
            }

            override fun visitInvokeDynamicInsn(
                name: String,
                descriptor: String,
                bootstrapMethodHandle: Handle,
                vararg bootstrapMethodArguments: Any?
            ) {
                referencedTypes.addAll(SignatureParser.parseMethod(descriptor).referencedTypes())
            }

            override fun visitTryCatchBlock(start: Label?, end: Label?, handler: Label?, type: String?) {
                if (type != null) {
                    referencedTypes.add(type)
                }
            }

            override fun visitTypeInsn(opcode: Int, type: String) {
                referencedTypes.addAll(SignatureParser.parseInternalType(type).referencedTypes())
            }
        }
    }
}

private class TypeDescriptorParser : ClassVisitor(ASM9) {
    private val builder = TypeDescriptor.Builder()
    private val methods = mutableListOf<MemberDescriptor>()
    private val fields = mutableListOf<MemberDescriptor>()

    fun get(): TypeDescriptor {
        builder.methods = methods
        builder.fields = fields

        return builder.build()
    }

    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>
    ) {
        builder.name = name
        builder.superName = superName
        builder.flavor = AttributeParser.flavor(access)
        builder.extensibility = AttributeParser.extensibility(access)
        builder.protection = AttributeParser.protection(access)
        builder.interfaces = interfaces.toList()
    }

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor? {
        methods.add(
            MemberDescriptor {
                ref = SymbolicReference {
                    this.name = name
                    this.signature = descriptor
                }
                declaration = AttributeParser.declaration(access)
                protection = AttributeParser.protection(access)
            }
        )

        return null
    }

    override fun visitField(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        value: Any?
    ): FieldVisitor? {
        fields.add(
            MemberDescriptor {
                ref = SymbolicReference {
                    this.name = name
                    this.signature = descriptor
                }
                declaration = AttributeParser.declaration(access)
                protection = AttributeParser.protection(access)
            }
        )

        return null
    }
}
