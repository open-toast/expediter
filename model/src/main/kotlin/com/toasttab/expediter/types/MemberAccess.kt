/*
 * Copyright (c) 2026 Toast Inc.
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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface MemberAccess {
    /**
     * The type via which the member is accessed. E.g. for `"a".hashCode()`, the target type is `String`.
     */
    val targetType: String

    /**
     * The type that declares the member, if found. E.g. for `"a".hashCode()`, the target type is `Object`.
     */
    val declaringType: String?

    /**
     * Symbolic reference identifying the member being accessed.
     */
    val ref: MemberSymbolicReference

    /**
     * The type of method access. E.g. for `"a".hashCode()`, the access is VIRTUAL,
     * and for `Integer.MAX_VALUE`, the access is STATIC.
     */
    val accessType: MemberAccessType

    fun withDeclaringType(declaringType: String): MemberAccess

    fun description(): String {
        return if (declaringType == null || declaringType == targetType) {
            "$targetType.$ref"
        } else {
            "$declaringType.$ref (via $targetType)"
        }
    }

    @Serializable
    @SerialName("method")
    data class MethodAccess(
        override val targetType: String,
        override val declaringType: String? = null,
        override val ref: MemberSymbolicReference,
        override val accessType: MethodAccessType
    ) : MemberAccess {
        override fun toString() = description()
        override fun withDeclaringType(declaringType: String) = copy(declaringType = declaringType)
    }

    @Serializable
    @SerialName("field")
    data class FieldAccess(
        override val targetType: String,
        override val declaringType: String? = null,
        override val ref: MemberSymbolicReference,
        override val accessType: FieldAccessType
    ) : MemberAccess {
        override fun toString() = description()
        override fun withDeclaringType(declaringType: String) = copy(declaringType = declaringType)
    }
}

interface MemberAccessType {
    fun isStatic(): Boolean
}

enum class MethodAccessType : MemberAccessType {
    INTERFACE,
    VIRTUAL,
    SPECIAL,
    STATIC,

    OTHER;

    override fun isStatic() = this == STATIC
}

enum class FieldAccessType : MemberAccessType {
    STATIC,
    INSTANCE;

    override fun isStatic() = this == STATIC
}
