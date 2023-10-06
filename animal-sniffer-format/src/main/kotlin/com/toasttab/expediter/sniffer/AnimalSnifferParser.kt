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

import com.toasttab.expediter.types.AccessDeclaration
import com.toasttab.expediter.types.AccessProtection
import com.toasttab.expediter.types.MemberDescriptor
import com.toasttab.expediter.types.MemberSymbolicReference
import com.toasttab.expediter.types.TypeDescriptor
import com.toasttab.expediter.types.TypeExtensibility
import com.toasttab.expediter.types.TypeFlavor
import java.io.InputStream
import java.util.zip.GZIPInputStream

/**
 * Parses AnimalSniffer signatures into TypeDescriptors.
 */
object AnimalSnifferParser {
    fun parse(stream: InputStream): List<TypeDescriptor> {
        return Deserializer(GZIPInputStream(stream)).deserialize().map {
            val members = it.signatures.map { sig ->
                val fieldIdx = sig.indexOf('#')

                if (fieldIdx >= 0) {
                    MemberDescriptor(
                        MemberSymbolicReference.FieldSymbolicReference(
                            sig.substring(0, fieldIdx),
                            sig.substring(fieldIdx + 1)
                        ),
                        AccessDeclaration.UNKNOWN,
                        AccessProtection.UNKNOWN
                    )
                } else {
                    val methodIdx = sig.indexOf('(')
                    MemberDescriptor(
                        MemberSymbolicReference.MethodSymbolicReference(
                            sig.substring(0, methodIdx),
                            sig.substring(methodIdx)
                        ),
                        AccessDeclaration.UNKNOWN,
                        AccessProtection.UNKNOWN
                    )
                }
            }

            TypeDescriptor(
                it.name,
                it.superClass,
                it.superInterfaces,
                members,
                AccessProtection.UNKNOWN,
                TypeFlavor.UNKNOWN,
                TypeExtensibility.UNKNOWN
            )
        }
    }
}
