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

package com.toasttab.expediter.sniffer

import protokt.v1.toasttab.expediter.v1.AccessDeclaration
import protokt.v1.toasttab.expediter.v1.AccessProtection
import protokt.v1.toasttab.expediter.v1.MemberDescriptor
import protokt.v1.toasttab.expediter.v1.SymbolicReference
import protokt.v1.toasttab.expediter.v1.TypeDescriptor
import protokt.v1.toasttab.expediter.v1.TypeExtensibility
import protokt.v1.toasttab.expediter.v1.TypeFlavor
import java.io.InputStream
import java.util.zip.GZIPInputStream

/**
 * Parses AnimalSniffer signatures into TypeDescriptors.
 */
object AnimalSnifferParser {
    fun parse(stream: InputStream): List<TypeDescriptor> {
        return Deserializer(GZIPInputStream(stream)).deserialize().map {
            val fields = mutableListOf<MemberDescriptor>()
            val methods = mutableListOf<MemberDescriptor>()

            for (sig in it.signatures) {
                val fieldIdx = sig.indexOf('#')

                if (fieldIdx >= 0) {
                    fields.add(
                        MemberDescriptor {
                            ref = SymbolicReference {
                                name = sig.substring(0, fieldIdx)
                                signature = sig.substring(fieldIdx + 1)
                            }
                            declaration = AccessDeclaration.UNKNOWN
                            protection = AccessProtection.UNKNOWN
                        }
                    )
                } else {
                    val methodIdx = sig.indexOf('(')
                    methods.add(
                        MemberDescriptor {
                            ref = SymbolicReference {
                                name = sig.substring(0, methodIdx)
                                signature = sig.substring(methodIdx)
                            }
                            declaration = AccessDeclaration.UNKNOWN
                            protection = AccessProtection.UNKNOWN
                        }
                    )
                }
            }

            TypeDescriptor {
                name = it.name
                superName = it.superClass
                interfaces = it.superInterfaces
                this.fields = fields
                this.methods = methods
                protection = AccessProtection.UNKNOWN
                flavor = TypeFlavor.UNKNOWN
                extensibility = TypeExtensibility.UNKNOWN
            }
        }
    }
}
