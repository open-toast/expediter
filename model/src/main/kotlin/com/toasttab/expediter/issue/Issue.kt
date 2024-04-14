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

package com.toasttab.expediter.issue

import com.toasttab.expediter.types.MemberAccess
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface Issue {
    val target: String?
    val caller: String?

    @Serializable
    @SerialName("duplicate-type")
    data class DuplicateType(override val target: String, val sources: List<String>) : Issue {
        override fun toString() = "duplicate class $target in $sources"

        override val caller = null
    }

    @Serializable
    @SerialName("type-missing")
    data class MissingType(override val caller: String, override val target: String) : Issue {
        override fun toString() = "$caller refers to missing type $target"
    }

    @Serializable
    @SerialName("application-supertype-missing")
    data class MissingApplicationSuperType(override val caller: String, val missing: Set<String>) : Issue {
        override fun toString() = "$caller extends missing ${missing.readable}"

        override val target = null
    }

    @Serializable
    @SerialName("application-supertype-final")
    data class FinalApplicationSuperType(override val caller: String, val final: Set<String>) : Issue {
        override fun toString() = "$caller extends final ${final.readable}"

        override val target = null
    }

    @Serializable
    @SerialName("supertype-missing")
    data class MissingSuperType(override val caller: String, override val target: String, val missing: Set<String>) : Issue {
        override fun toString() = "$caller refers to type $target with missing super${missing.readable}"
    }

    interface WithMemberAccess {
        val member: MemberAccess
    }

    @Serializable
    @SerialName("member-missing")
    data class MissingMember(override val caller: String, override val member: MemberAccess) : Issue, WithMemberAccess {
        override val target: String get() = member.targetType
        override fun toString() = "$caller accesses missing $member"
    }

    @Serializable
    @SerialName("static-member")
    data class AccessStaticMemberNonStatically(override val caller: String, override val member: MemberAccess) : Issue, WithMemberAccess {
        override val target: String get() = member.targetType

        override fun toString() = "$caller accesses static $member non-statically"
    }

    @Serializable
    @SerialName("instance-member")
    data class AccessInstanceMemberStatically(override val caller: String, override val member: MemberAccess) : Issue, WithMemberAccess {
        override val target: String get() = member.targetType
        override fun toString() = "$caller accesses instance $member statically"
    }

    @Serializable
    @SerialName("member-inaccessible")
    data class AccessInaccessibleMember(override val caller: String, override val member: MemberAccess) : Issue, WithMemberAccess {
        override val target: String get() = member.targetType
        override fun toString() = "$caller accesses inaccessible $member"
    }

    @Serializable
    data class UnknownIssue internal constructor(
        val type: String,
        override val caller: String? = null,
        override val target: String? = null
    ) : Issue
}

private val Collection<String>.readable: String get() = if (size == 1) {
    "type " + first()
} else {
    "types " + joinToString(", ")
}
