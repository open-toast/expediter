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

package com.toasttab.expediter.types

import protokt.v1.toasttab.expediter.v1.AccessDeclaration
import protokt.v1.toasttab.expediter.v1.AccessProtection
import protokt.v1.toasttab.expediter.v1.MemberDescriptor
import protokt.v1.toasttab.expediter.v1.SymbolicReference
import protokt.v1.toasttab.expediter.v1.TypeDescriptor
import protokt.v1.toasttab.expediter.v1.TypeExtensibility
import protokt.v1.toasttab.expediter.v1.TypeFlavor

/**
 * Creates type descriptors for arrays. Per https://docs.oracle.com/javase/specs/jls/se7/html/jls-10.html#jls-10.7,
 * arrays implement `Cloneable` and `Serializable` and have two members:
 * ```
 * public final int length;
 * public Object clone();
 * ```
 */
object ArrayDescriptor {
    private val INTERFACES = listOf("java/lang/Cloneable", "java/io/Serializable")

    private val FIELDS = listOf(
        MemberDescriptor {
            ref = SymbolicReference {
                name = "length"
                signature = "I"
            }
            protection = AccessProtection.PUBLIC
            declaration = AccessDeclaration.INSTANCE
        }
    )

    private val METHODS = listOf(
        MemberDescriptor {
            ref = SymbolicReference {
                name = "clone"
                signature = "()Ljava/lang/Object;"
            }
            protection = AccessProtection.PUBLIC
            declaration = AccessDeclaration.INSTANCE
        }
    )

    fun isArray(typeName: String) = typeName.startsWith("[")
    fun create(typeName: String) = TypeDescriptor {
        name = typeName
        superName = "java/lang/Object"
        interfaces = INTERFACES
        protection = AccessProtection.PUBLIC
        flavor = TypeFlavor.CLASS
        extensibility = TypeExtensibility.FINAL
        fields = FIELDS
        methods = METHODS
    }
}
