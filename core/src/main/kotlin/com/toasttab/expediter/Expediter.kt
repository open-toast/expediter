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
import com.toasttab.expediter.types.AccessDeclaration
import com.toasttab.expediter.types.ApplicationType
import com.toasttab.expediter.types.ApplicationTypeWithResolvedHierarchy
import com.toasttab.expediter.types.InspectedTypes
import com.toasttab.expediter.types.MemberAccess
import com.toasttab.expediter.types.MemberDescriptor
import com.toasttab.expediter.types.MemberType
import com.toasttab.expediter.types.MethodAccessType
import com.toasttab.expediter.types.PlatformTypeProvider
import com.toasttab.expediter.types.ResolvedOptionalTypeHierarchy
import com.toasttab.expediter.types.ResolvedTypeHierarchy
import com.toasttab.expediter.types.TypeDescriptor
import com.toasttab.expediter.types.TypeExtensibility
import com.toasttab.expediter.types.TypeFlavor

class Expediter(
    private val ignore: Ignore,
    private val applicationTypesProvider: ApplicationTypesProvider,
    private val platformTypeProvider: PlatformTypeProvider
) {
    fun findIssues(): Set<Issue> {
        val inspectedTypes = InspectedTypes(applicationTypesProvider.types(), platformTypeProvider)
        return (
                inspectedTypes.classes.flatMap { appType ->
                    val h = inspectedTypes.resolveHierarchy(appType.type)

                    val issues = mutableListOf<Issue>()

                    when (h) {
                        is ResolvedTypeHierarchy.IncompleteTypeHierarchy -> {
                            issues.add(
                                Issue.MissingApplicationSuperType(
                                    appType.name,
                                    h.missingType.map { it.name }.toSet()
                                )
                            )
                        }

                        is ResolvedTypeHierarchy.CompleteTypeHierarchy -> {
                            val finalSupertypes =
                                h.superTypes.filter { it.extensibility == TypeExtensibility.FINAL }.toList()
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

                    val typeWithHierarchy = ApplicationTypeWithResolvedHierarchy(appType, h)

                    issues + appType.refs.mapNotNull { access ->
                        if (!ignore.ignore(appType.name, access.targetType, access.ref)) {
                            findIssue(typeWithHierarchy, access, inspectedTypes.resolveHierarchy(access.targetType))
                        } else {
                            null
                        }
                    }
                } + inspectedTypes.duplicateTypes.filter {
                    !ignore.ignore(null, it.target, null)
                }
                ).toSet()
    }
}

fun <M : MemberType> findIssue(type: ApplicationTypeWithResolvedHierarchy, access: MemberAccess<M>, chain: ResolvedOptionalTypeHierarchy): Issue? {
    return when (chain) {
        is ResolvedTypeHierarchy.IncompleteTypeHierarchy -> Issue.MissingSuperType(
            type.name,
            access.targetType,
            chain.missingType.map { it.name }.toSet()
        )

        is ResolvedOptionalTypeHierarchy.NoType -> Issue.MissingType(type.name, access.targetType)
        is ResolvedTypeHierarchy.CompleteTypeHierarchy -> {
            val member = chain.findMember(access)

            if (member == null) {
                Issue.MissingMember(type.name, access)
            } else {
                val resolvedAccess = access.withDeclaringType(member.declaringType.name)

                if (member.member.declaration == AccessDeclaration.STATIC && !access.accessType.isStatic()) {
                    Issue.AccessStaticMemberNonStatically(type.name, resolvedAccess)
                } else if (member.member.declaration == AccessDeclaration.INSTANCE && access.accessType.isStatic()) {
                    Issue.AccessInstanceMemberStatically(type.name, resolvedAccess)
                } else if (!AccessCheck.allowedAccess(type, chain.type, member.member)) {
                    Issue.AccessInaccessibleMember(type.name, resolvedAccess)
                } else {
                    null
                }
            }
        }
    }
}
private class MemberWithDeclaringType<M : MemberType> (
    val member: MemberDescriptor<M>,
    val declaringType: TypeDescriptor
)

private fun <M : MemberType> ResolvedTypeHierarchy.CompleteTypeHierarchy.findMember(access: MemberAccess<M>): MemberWithDeclaringType<M>? {
    val classes = if (access !is MemberAccess.MethodAccess ||
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

    for (cls in classes) {
        for (m in cls.members) {
            if (access.ref == m.ref) {
                return MemberWithDeclaringType(m as MemberDescriptor<M>, cls)
            }
        }
    }

    return null
}
