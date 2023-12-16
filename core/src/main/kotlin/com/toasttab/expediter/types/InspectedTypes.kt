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
import com.toasttab.expediter.parser.SignatureParser
import com.toasttab.expediter.parser.TypeSignature
import com.toasttab.expediter.provider.PlatformTypeProvider
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
                types.compute(type.name) { _, v ->
                    if (v == null) {
                        type
                    } else {
                        val a = duplicates[type.name]
                        if (a != null) {
                            a.add(type.source)
                        } else {
                            duplicates[type.name] = mutableListOf(type.source, v.source)
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
    private val inspectedCache: ConcurrentMap<String, Type> = ConcurrentHashMap(appTypes.appTypes)

    private val hierarchyCache: MutableMap<String, TypeHierarchy> = hashMapOf()

    constructor(all: List<ApplicationType>, platformTypeProvider: PlatformTypeProvider) : this(ApplicationTypeContainer.create(all), platformTypeProvider)

    private fun lookup(signature: TypeSignature): Type? {
        return if (signature.isArray()) {
            if (signature.primitive || lookup(signature.scalarSignature()) != null) {
                PlatformType(ArrayDescriptor.create(signature.name))
            } else {
                null
            }
        } else {
            inspectedCache[signature.scalarName] ?: inspectedCache.computeIfAbsent(signature.scalarName) { _ ->
                platformTypeProvider.lookupPlatformType(signature.scalarName)?.let(::PlatformType)
            }
        }
    }

    private fun traverse(type: Type): TypeHierarchy {
        val cached = hierarchyCache[type.name]

        if (cached == null) {
            val superTypes = mutableSetOf<OptionalType>()
            val superTypeNames = mutableListOf<String>()

            type.descriptor.superName?.let { superTypeNames.add(it) }
            superTypeNames.addAll(type.descriptor.interfaces)

            for (superName in superTypeNames) {
                val signature = SignatureParser.parseInternalType(superName)
                val superType = lookup(signature)

                if (superType == null) {
                    superTypes.add(OptionalType.MissingType(signature.name))
                } else {
                    val hierarchy = traverse(superType)
                    superTypes.add(OptionalType.PresentType(superType))
                    superTypes.addAll(hierarchy.superTypes)
                }
            }

            return TypeHierarchy(type, superTypes).also {
                hierarchyCache[type.name] = it
            }
        } else {
            return cached
        }
    }

    fun resolveHierarchy(type: String): OptionalResolvedTypeHierarchy {
        val signature = SignatureParser.parseInternalType(type)
        return lookup(signature)?.let { resolveHierarchy(it) } ?: OptionalResolvedTypeHierarchy.NoType(signature.name)
    }

    fun resolveHierarchy(type: Type): ResolvedTypeHierarchy {
        return traverse(type).resolve()
    }

    val classes: Collection<ApplicationType> get() = appTypes.appTypes.values
    val duplicateTypes: Collection<Issue.DuplicateType> get() = appTypes.duplicates
}
