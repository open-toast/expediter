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

import org.objectweb.asm.Opcodes
import protokt.v1.toasttab.expediter.v1.AccessDeclaration
import protokt.v1.toasttab.expediter.v1.AccessProtection
import protokt.v1.toasttab.expediter.v1.TypeExtensibility
import protokt.v1.toasttab.expediter.v1.TypeFlavor

object AttributeParser {
    fun flavor(access: Int) = if (access and Opcodes.ACC_INTERFACE != 0) TypeFlavor.INTERFACE else TypeFlavor.CLASS

    fun protection(access: Int) =
        if (access and Opcodes.ACC_PRIVATE != 0) {
            AccessProtection.PRIVATE
        } else if (access and Opcodes.ACC_PROTECTED != 0) {
            AccessProtection.PROTECTED
        } else if (access and Opcodes.ACC_PUBLIC != 0) {
            AccessProtection.PUBLIC
        } else {
            AccessProtection.PACKAGE_PRIVATE
        }

    fun declaration(access: Int) =
        if (access and Opcodes.ACC_STATIC != 0) {
            AccessDeclaration.STATIC
        } else {
            AccessDeclaration.INSTANCE
        }

    fun extensibility(access: Int) =
        if (access and Opcodes.ACC_FINAL != 0) {
            TypeExtensibility.FINAL
        } else {
            TypeExtensibility.NOT_FINAL
        }
}
