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

class MemberDescriptor<M : MemberType>(
    val ref: MemberSymbolicReference<M>,
    val declaration: AccessDeclaration,
    val protection: AccessProtection
)

enum class TypeFlavor {
    CLASS, INTERFACE, UNKNOWN
}

enum class TypeExtensibility {
    FINAL, NOT_FINAL, UNKNOWN
}

interface IdentifiesType {
    val name: String
}

/**
 * Represents declared properties (fields / methods / directly extended supertypes) of a type.
 */
class TypeDescriptor (
    override val name: String,
    val superName: String?,
    val interfaces: List<String>,

    val members: List<MemberDescriptor<*>>,
    val protection: AccessProtection,
    val flavor: TypeFlavor,
    val extensibility: TypeExtensibility
) : IdentifiesType

/**
 * Represents declared properties of a type and all fields / methods that type's code accesses / invokes.
 */
class ApplicationType(
    val type: TypeDescriptor,
    val refs: Set<MemberAccess<*>>,
    val source: String
) : IdentifiesType by type {
    override fun toString() = "ApplicationType[${type.name}]"
}
