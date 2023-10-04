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
import com.toasttab.expediter.types.InspectedTypes
import com.toasttab.expediter.types.MemberAccess
import com.toasttab.expediter.types.MemberDescriptor
import com.toasttab.expediter.types.MemberSymbolicReference
import com.toasttab.expediter.types.MemberType
import com.toasttab.expediter.types.PlatformTypeProvider
import com.toasttab.expediter.types.TypeDescriptor
import com.toasttab.expediter.types.TypeHierarchy

class Expediter(
    private val ignore: Ignore,
    private val applicationTypesProvider: ApplicationTypesProvider,
    private val platformTypeProvider: PlatformTypeProvider
) {
    fun findIssues(): Set<Issue> {
        val inspectedTypes = InspectedTypes(applicationTypesProvider.types(), platformTypeProvider)

        return (
            inspectedTypes.classes.flatMap { cls ->
                cls.refs.mapNotNull { access ->
                    if (!ignore.ignore(cls.type.name, access.targetType, access.ref)) {
                        findIssue(cls.type, access, inspectedTypes.hierarchy(access))
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

fun <M : MemberType> findIssue(type: TypeDescriptor, access: MemberAccess<M>, chain: TypeHierarchy): Issue? {
    return when (chain) {
        is TypeHierarchy.IncompleteTypeHierarchy -> Issue.MissingSuperType(
            type.name,
            access.targetType,
            chain.missingType.map { it.name }.toSet()
        )

        is TypeHierarchy.NoType -> Issue.MissingType(type.name, access.targetType)
        is TypeHierarchy.CompleteTypeHierarchy -> {
            val member = chain.findMember(access.ref)

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

private fun <M : MemberType> TypeHierarchy.CompleteTypeHierarchy.findMember(ref: MemberSymbolicReference<M>): MemberWithDeclaringType<M>? {
    for (cls in classes) {
        for (m in cls.members) {
            if (ref == m.ref) {
                return MemberWithDeclaringType(m as MemberDescriptor<M>, cls)
            }
        }
    }

    return null
}
