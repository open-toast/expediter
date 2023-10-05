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
class TypeHierarchy(
    val type: TypeDescriptor,
    val superTypes: Set<OptionalType>
) {
    fun resolve(): ResolvedTypeHierarchy {
        val missing = superTypes.filterIsInstance<OptionalType.MissingType>()
        return if (missing.isNotEmpty()) {
            ResolvedTypeHierarchy.IncompleteTypeHierarchy(type, missing.toSet())
        } else {
            ResolvedTypeHierarchy.CompleteTypeHierarchy(
                type,
                superTypes.asSequence().filterIsInstance<OptionalType.Type>().map { it.cls }
            )
        }
    }
}

/**
 * Sum type of [TypeDescriptor | MissingType = type not found on app's classpath]
 */
sealed class OptionalType {
    abstract val name: String

    class Type(val cls: TypeDescriptor) : OptionalType() {
        override val name: String
            get() = cls.name
    }

    class MissingType(override val name: String) : OptionalType()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OptionalType

        return name == other.name
    }

    override fun hashCode() = name.hashCode()
}

sealed interface ResolvedOptionalTypeHierarchy {
    object NoType : ResolvedOptionalTypeHierarchy
}

sealed interface ResolvedTypeHierarchy : ResolvedOptionalTypeHierarchy {
    class IncompleteTypeHierarchy(val type: TypeDescriptor, val missingType: Set<OptionalType.MissingType>) : ResolvedTypeHierarchy
    class CompleteTypeHierarchy(val type: TypeDescriptor, val superTypes: Sequence<TypeDescriptor>) : ResolvedTypeHierarchy {
        val allTypes: Sequence<TypeDescriptor> get() = sequenceOf(type) + superTypes
    }
}

class ApplicationTypeWithResolvedHierarchy(
    val appType: ApplicationType,
    val hierarchy: ResolvedTypeHierarchy
) : IdentifiesType by appType
