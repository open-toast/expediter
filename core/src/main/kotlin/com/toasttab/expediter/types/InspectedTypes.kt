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

import com.toasttab.expediter.issue.Issue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

sealed class TypeHierarchy {
    object NoType : TypeHierarchy()
    class IncompleteTypeHierarchy(val type: TypeDescriptor, val missingType: Set<MaybeType.MissingType>) : TypeHierarchy()
    class CompleteTypeHierarchy(val type: TypeDescriptor, val classes: Sequence<TypeDescriptor>) : TypeHierarchy()
}

private class ApplicationTypeContainer(
    val appTypes: Map<String, ApplicationType>,
    val duplicates: List<Issue.DuplicateType>
) {
    companion object {
        fun create(all: List<ApplicationType>): ApplicationTypeContainer {
            val duplicates = HashMap<String, MutableList<String>>()
            val types = HashMap<String, ApplicationType>()

            for (type in all) {
                types.compute(type.type.name) { _, v ->
                    if (v == null) {
                        type
                    } else {
                        val a = duplicates[type.type.name]
                        if (a != null) {
                            a.add(type.source)
                        } else {
                            duplicates[type.type.name] = mutableListOf(type.source, v.source)
                        }

                        v
                    }
                }
            }

            return ApplicationTypeContainer(types, duplicates.map { (k, v) -> Issue.DuplicateType(k, v) })
        }
    }
}

class InspectedTypes private constructor(
    private val appTypes: ApplicationTypeContainer,
    private val platformTypeProvider: PlatformTypeProvider
) {
    private val inspectedCache: ConcurrentMap<String, TypeDescriptor> = appTypes.appTypes.mapValuesTo(ConcurrentHashMap()) {
        it.value.type
    }

    private val hierarchyCache: MutableMap<TypeDescriptor, TypeWithHierarchy> = hashMapOf()

    constructor(all: List<ApplicationType>, platformTypeProvider: PlatformTypeProvider) : this(ApplicationTypeContainer.create(all), platformTypeProvider)

    private fun lookup(s: String): TypeDescriptor? {
        return inspectedCache[s] ?: inspectedCache.computeIfAbsent(s) { s ->
            if (s.startsWith("[")) {
                // TODO: make up a type descriptor for an array type; we could validate that the element type actually exists
                TypeDescriptor(s, "java/lang/Object", emptyList(), emptyList(), AccessProtection.UNKNOWN, TypeFlavor.CLASS)
            } else {
                platformTypeProvider.lookupPlatformType(s)
            }
        }
    }

    private fun traverse(cls: TypeDescriptor): TypeWithHierarchy {
        val loaded = hierarchyCache[cls]

        if (loaded == null) {
            val superTypes = mutableSetOf<MaybeType>()
            val superTypeNames = mutableListOf<String>()

            superTypeNames.addAll(cls.interfaces)
            cls.superName?.let { superTypeNames.add(it) }

            for (s in superTypeNames) {
                val l = lookup(s)

                if (l == null) {
                    superTypes.add(MaybeType.MissingType(s))
                } else {
                    val loaded = traverse(l)
                    superTypes.add(MaybeType.Type(l))
                    superTypes.addAll(loaded.superTypes)
                }
            }

            return TypeWithHierarchy(cls, superTypes).also {
                hierarchyCache[cls] = it
            }
        } else {
            return loaded
        }
    }

    fun hierarchy(access: MemberAccess<*>) = when (access) {
        is MemberAccess.FieldAccess -> hierarchy(access)
        is MemberAccess.MethodAccess -> hierarchy(access)
    }

    private fun hierarchy(access: MemberAccess.FieldAccess): TypeHierarchy {
        val cls = lookup(access.targetType)?.let { traverse(it) }

        return if (cls == null) {
            TypeHierarchy.NoType
        } else {
            val missing = cls.superTypes.filterIsInstance<MaybeType.MissingType>()
            if (missing.isNotEmpty()) {
                TypeHierarchy.IncompleteTypeHierarchy(cls.cls, missing.toSet())
            } else {
                TypeHierarchy.CompleteTypeHierarchy(cls.cls, sequenceOf(cls.cls) + cls.superTypes.asSequence().filterIsInstance<MaybeType.Type>().map { it.cls })
            }
        }
    }

    private fun hierarchy(access: MemberAccess.MethodAccess): TypeHierarchy {
        val cls = lookup(access.targetType)?.let { traverse(it) }

        return if (cls == null) {
            TypeHierarchy.NoType
        } else {
            val missing = cls.superTypes.filterIsInstance<MaybeType.MissingType>()
            if (missing.isNotEmpty()) {
                TypeHierarchy.IncompleteTypeHierarchy(cls.cls, missing.toSet())
            } else if (access.accessType == MethodAccessType.VIRTUAL ||
                access.accessType == MethodAccessType.STATIC ||
                access.accessType == MethodAccessType.SPECIAL && !access.ref.isConstructor()
            ) {
                // invokevirtual / static / special (except for constructors) may refer to
                // a method declared on target type or target type's supertypes
                TypeHierarchy.CompleteTypeHierarchy(
                    cls.cls,
                    sequenceOf(cls.cls) + cls.superTypes.asSequence().filterIsInstance<MaybeType.Type>().map { it.cls }
                )
            } else if (access.accessType == MethodAccessType.INTERFACE) {
                // same story for invokeinterface, but method must be present on an interface type
                TypeHierarchy.CompleteTypeHierarchy(
                    cls.cls,
                    sequenceOf(cls.cls) + cls.superTypes.asSequence()
                        .filterIsInstance<MaybeType.Type>()
                        .map { it.cls }
                        .filter { it.flavor != TypeFlavor.CLASS }
                )
            } else {
                // constructor must be present on the target type
                // TODO: this will catch non-constructor cases, like indy, need to handle or ignore them properly
                TypeHierarchy.CompleteTypeHierarchy(cls.cls, sequenceOf(cls.cls))
            }
        }
    }

    val classes: Collection<ApplicationType> get() = appTypes.appTypes.values
    val duplicateTypes: Collection<Issue> get() = appTypes.duplicates
}
