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

import com.toasttab.expediter.ignore.Ignore
import com.toasttab.expediter.issue.Issue
import com.toasttab.expediter.types.ApplicationType
import com.toasttab.expediter.types.InspectedTypes
import com.toasttab.expediter.types.MemberAccess
import com.toasttab.expediter.types.MemberType
import com.toasttab.expediter.types.MethodAccessType
import com.toasttab.expediter.types.OptionalResolvedTypeHierarchy
import com.toasttab.expediter.types.PlatformTypeProvider
import com.toasttab.expediter.types.ResolvedTypeHierarchy
import com.toasttab.expediter.types.members
import protokt.v1.toasttab.expediter.v1.AccessDeclaration
import protokt.v1.toasttab.expediter.v1.MemberDescriptor
import protokt.v1.toasttab.expediter.v1.TypeDescriptor
import protokt.v1.toasttab.expediter.v1.TypeExtensibility
import protokt.v1.toasttab.expediter.v1.TypeFlavor

class Expediter(
    private val ignore: Ignore,
    private val applicationTypesProvider: ApplicationTypesProvider,
    private val platformTypeProvider: PlatformTypeProvider
) {
    private val inspectedTypes: InspectedTypes by lazy {
        InspectedTypes(applicationTypesProvider.types(), platformTypeProvider)
    }

    private fun findIssues(appType: ApplicationType): Collection<Issue> {
        val hierarchy = inspectedTypes.resolveHierarchy(appType.type)
        val issues = mutableListOf<Issue>()

        when (hierarchy) {
            is ResolvedTypeHierarchy.IncompleteTypeHierarchy -> {
                issues.add(
                    Issue.MissingApplicationSuperType(
                        appType.name,
                        hierarchy.missingType.map { it.name }.toSet()
                    )
                )
            }

            is ResolvedTypeHierarchy.CompleteTypeHierarchy -> {
                val finalSupertypes = hierarchy.superTypes.filter { it.extensibility == TypeExtensibility.FINAL }
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

        issues.addAll(
            appType.refs.mapNotNull { access ->
                findIssue(appType, hierarchy, access, inspectedTypes.resolveHierarchy(access.targetType))
            }
        )

        return issues
    }

    fun findIssues(): Set<Issue> {
        return (
            inspectedTypes.classes.flatMap { appType ->
                findIssues(appType)
            } + inspectedTypes.duplicateTypes
            ).filter { !ignore.ignore(it) }.toSet()
    }
}

fun <M : MemberType> findIssue(type: ApplicationType, hierarchy: ResolvedTypeHierarchy, access: MemberAccess<M>, chain: OptionalResolvedTypeHierarchy): Issue? {
    return when (chain) {
        is ResolvedTypeHierarchy.IncompleteTypeHierarchy -> Issue.MissingSuperType(
            type.name,
            access.targetType,
            chain.missingType.map { it.name }.toSet()
        )

        is OptionalResolvedTypeHierarchy.NoType -> Issue.MissingType(type.name, access.targetType)
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
private class MemberWithDeclaringType(
    val member: MemberDescriptor,
    val declaringType: TypeDescriptor
)

private fun <M : MemberType> ResolvedTypeHierarchy.CompleteTypeHierarchy.filterToAccessType(access: MemberAccess<M>): Sequence<TypeDescriptor> {
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
        allTypes.filter { it.flavor != TypeFlavor.CLASS }
    } else {
        // constructors must always be declared by the type being constructed
        sequenceOf(type)
    }
}

private fun <M : MemberType> ResolvedTypeHierarchy.CompleteTypeHierarchy.resolveMember(access: MemberAccess<M>): MemberWithDeclaringType? {
    for (cls in filterToAccessType(access)) {
        for (m in cls.members) {
            if (access.ref.same(m.ref)) {
                return MemberWithDeclaringType(m, cls)
            }
        }
    }

    return null
}
