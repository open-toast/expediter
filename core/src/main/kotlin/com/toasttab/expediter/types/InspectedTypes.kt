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
import protokt.v1.toasttab.expediter.v1.TypeDescriptor
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

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

    private val hierarchyCache: MutableMap<TypeDescriptor, TypeHierarchy> = hashMapOf()

    constructor(all: List<ApplicationType>, platformTypeProvider: PlatformTypeProvider) : this(ApplicationTypeContainer.create(all), platformTypeProvider)

    private fun lookup(typeName: String): TypeDescriptor? {
        return inspectedCache[typeName] ?: inspectedCache.computeIfAbsent(typeName) { _ ->
            if (ArrayDescriptor.isArray(typeName)) {
                ArrayDescriptor.create(typeName)
            } else {
                platformTypeProvider.lookupPlatformType(typeName)
            }
        }
    }

    private fun traverse(cls: TypeDescriptor): TypeHierarchy {
        val cached = hierarchyCache[cls]

        if (cached == null) {
            val superTypes = mutableSetOf<OptionalType>()
            val superTypeNames = mutableListOf<String>()

            cls.superName?.let { superTypeNames.add(it) }
            superTypeNames.addAll(cls.interfaces)

            for (s in superTypeNames) {
                val l = lookup(s)

                if (l == null) {
                    superTypes.add(OptionalType.MissingType(s))
                } else {
                    val hierarchy = traverse(l)
                    superTypes.add(OptionalType.Type(l))
                    superTypes.addAll(hierarchy.superTypes)
                }
            }

            return TypeHierarchy(cls, superTypes).also {
                hierarchyCache[cls] = it
            }
        } else {
            return cached
        }
    }

    fun resolveHierarchy(type: String): OptionalResolvedTypeHierarchy {
        return lookup(type)?.let { resolveHierarchy(it) } ?: OptionalResolvedTypeHierarchy.NoType
    }

    fun resolveHierarchy(type: TypeDescriptor): ResolvedTypeHierarchy {
        return traverse(type).resolve()
    }

    val classes: Collection<ApplicationType> get() = appTypes.appTypes.values
    val duplicateTypes: Collection<Issue.DuplicateType> get() = appTypes.duplicates
}
