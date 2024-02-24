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

import protokt.v1.toasttab.expediter.v1.MemberDescriptor
import protokt.v1.toasttab.expediter.v1.TypeDescriptor

interface IdentifiesType {
    val name: String
}

sealed interface Type : IdentifiesType {
    val descriptor: TypeDescriptor

    override val name: String get() = descriptor.name
}

/**
 * Represents declared properties of a type and all fields / methods that type's code accesses / invokes.
 */
class ApplicationType(
    override val descriptor: TypeDescriptor,
    val memberAccess: Set<MemberAccess<*>>,
    val referencedTypes: Set<String>,
    val source: ClassfileSource
) : Type {
    override fun toString() = "ApplicationType[$name]"
}

class PlatformType(
    override val descriptor: TypeDescriptor
) : Type

val TypeDescriptor.members: Sequence<MemberDescriptor> get() = fields.asSequence() + methods.asSequence()
