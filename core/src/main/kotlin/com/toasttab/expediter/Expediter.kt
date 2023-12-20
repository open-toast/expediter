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

package com.toasttab.expediter

import com.toasttab.expediter.access.AccessCheck
import com.toasttab.expediter.ignore.Ignore
import com.toasttab.expediter.issue.Issue
import com.toasttab.expediter.provider.ApplicationTypesProvider
import com.toasttab.expediter.provider.PlatformTypeProvider
import com.toasttab.expediter.types.ApplicationType
import com.toasttab.expediter.types.ApplicationTypeContainer
import com.toasttab.expediter.types.InspectedTypes
import com.toasttab.expediter.types.MemberAccess
import com.toasttab.expediter.types.MemberType
import com.toasttab.expediter.types.MethodAccessType
import com.toasttab.expediter.types.OptionalResolvedTypeHierarchy
import com.toasttab.expediter.types.PlatformType
import com.toasttab.expediter.types.ResolvedTypeHierarchy
import com.toasttab.expediter.types.Type
import com.toasttab.expediter.types.members
import protokt.v1.toasttab.expediter.v1.AccessDeclaration
import protokt.v1.toasttab.expediter.v1.MemberDescriptor
import protokt.v1.toasttab.expediter.v1.TypeExtensibility
import protokt.v1.toasttab.expediter.v1.TypeFlavor

class Expediter(
    private val ignore: Ignore,
    private val appTypes: ApplicationTypeContainer,
    private val platformTypeProvider: PlatformTypeProvider
) {
    constructor(
        ignore: Ignore,
        appTypes: ApplicationTypesProvider,
        platformTypeProvider: PlatformTypeProvider
    ) : this(ignore, ApplicationTypeContainer.create(appTypes.types()), platformTypeProvider)

    private val inspectedTypes: InspectedTypes by lazy {
        InspectedTypes(appTypes, platformTypeProvider)
    }

    private fun findIssues(appType: ApplicationType): Collection<Issue> {
        val hierarchy = inspectedTypes.resolveHierarchy(appType)
        val issues = mutableListOf<Issue>()

        val missingApplicationSupertypes = hashSetOf<String>()

        when (hierarchy) {
            is ResolvedTypeHierarchy.IncompleteTypeHierarchy -> {
                hierarchy.missingType.mapTo(missingApplicationSupertypes) { it.name }

                issues.add(
                    Issue.MissingApplicationSuperType(
                        appType.name,
                        missingApplicationSupertypes
                    )
                )
            }

            is ResolvedTypeHierarchy.CompleteTypeHierarchy -> {
                val finalSupertypes = hierarchy.superTypes.filter { it.descriptor.extensibility == TypeExtensibility.FINAL }
                    .toList()
                if (finalSupertypes.isNotEmpty()) {
                    issues.add(
                        Issue.FinalApplicationSuperType(
                            appType.name,
                            finalSupertypes.map { it.name }.toSet()
                        )
                    )
                }
            }
        }

        val missingTypes = hashSetOf<String>()

        for (refType in appType.referencedTypes) {
            val chain = inspectedTypes.resolveHierarchy(refType)

            if (chain is OptionalResolvedTypeHierarchy.NoType) {
                missingTypes.add(refType)
            } else if (chain is ResolvedTypeHierarchy.IncompleteTypeHierarchy && chain.type is PlatformType) {
                issues.add(Issue.MissingSuperType(appType.name, refType, chain.missingType.map { it.name }.toSet()))
            }
        }

        issues.addAll(missingTypes.map { Issue.MissingType(appType.name, it) })

        issues.addAll(
            appType.memberAccess.mapNotNull { access ->
                findIssue(appType, hierarchy, access, inspectedTypes.resolveHierarchy(access.targetType))
            }
        )

        return issues.filter {
            when (it) {
                // if application type A extends a missing type B and refers to it otherwise
                // (which is typically via the super constructor), only report the missing supertype issue
                is Issue.MissingType -> !missingApplicationSupertypes.contains(it.target)
                // if application type A refers to a type B with a missing supertype C
                // and also refers to C directly, only report the latter
                is Issue.MissingSuperType -> !missingTypes.containsAll(it.missing)
                else -> true
            }
        }
    }

    fun findIssues(): Set<Issue> {
        return (
            inspectedTypes.classes.flatMap { appType ->
                findIssues(appType)
            } + inspectedTypes.duplicateTypes
            ).filter { !ignore.ignore(it) }
            .toSet()
    }

    fun <M : MemberType> findIssue(
        type: ApplicationType,
        hierarchy: ResolvedTypeHierarchy,
        access: MemberAccess<M>,
        chain: OptionalResolvedTypeHierarchy
    ): Issue? {
        return when (chain) {
            is OptionalResolvedTypeHierarchy.NoType -> Issue.MissingType(type.name, access.targetType)

            is ResolvedTypeHierarchy.IncompleteTypeHierarchy -> {
                if (chain.type is PlatformType) {
                    Issue.MissingSuperType(
                        type.name,
                        access.targetType,
                        chain.missingType.map { it.name }.toSet()
                    )
                } else {
                    // missing supertypes of application types will be reported separately
                    null
                }
            }

            is ResolvedTypeHierarchy.CompleteTypeHierarchy -> {
                val member = chain.resolveMember(access)

                if (member == null) {
                    Issue.MissingMember(type.name, access)
                } else {
                    val resolvedAccess = access.withDeclaringType(member.declaringType.name)

                    if (member.member.declaration == AccessDeclaration.STATIC && !access.accessType.isStatic()) {
                        Issue.AccessStaticMemberNonStatically(type.name, resolvedAccess)
                    } else if (member.member.declaration == AccessDeclaration.INSTANCE && access.accessType.isStatic()) {
                        Issue.AccessInstanceMemberStatically(type.name, resolvedAccess)
                    } else if (!AccessCheck.allowedAccess(hierarchy, chain, member.member, member.declaringType)) {
                        Issue.AccessInaccessibleMember(type.name, resolvedAccess)
                    } else {
                        null
                    }
                }
            }
        }
    }
}
private class MemberWithDeclaringType(
    val member: MemberDescriptor,
    val declaringType: Type
)

private fun <M : MemberType> ResolvedTypeHierarchy.CompleteTypeHierarchy.filterToAccessType(access: MemberAccess<M>): Sequence<Type> {
    return if (access !is MemberAccess.MethodAccess ||
        access.accessType == MethodAccessType.VIRTUAL ||
        access.accessType == MethodAccessType.STATIC ||
        (access.accessType == MethodAccessType.SPECIAL && !access.ref.isConstructor())
    ) {
        // fields and methods, except for constructors and methods invoked via invokeinterface
        // can be declared on any type in the hierarchy
        allTypes
    } else if (access.accessType == MethodAccessType.INTERFACE) {
        // methods invoked via invokeinterface must be declared on an interface
        allTypes.filter { it.descriptor.flavor != TypeFlavor.CLASS }
    } else {
        // constructors must always be declared by the type being constructed
        sequenceOf(type)
    }
}

private fun <M : MemberType> ResolvedTypeHierarchy.CompleteTypeHierarchy.resolveMember(access: MemberAccess<M>): MemberWithDeclaringType? {
    for (cls in filterToAccessType(access)) {
        for (m in cls.descriptor.members) {
            if (access.ref.same(m.ref)) {
                return MemberWithDeclaringType(m, cls)
            }
        }
    }

    return null
}
