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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface MemberAccess<M : MemberType> {
    val owner: String
    val ref: MemberSymbolicReference<M>
    val accessType: MemberAccessType

    @Serializable
    @SerialName("method")
    data class MethodAccess(
        override val owner: String,
        override val ref: MemberSymbolicReference.MethodSymbolicReference,
        override val accessType: MethodAccessType
    ) : MemberAccess<MemberType.Method> {
        override fun toString() = "${ref.type} $owner.$ref"
    }

    @Serializable
    @SerialName("field")
    data class FieldAccess(
        override val owner: String,
        override val ref: MemberSymbolicReference.FieldSymbolicReference,
        override val accessType: FieldAccessType
    ) : MemberAccess<MemberType.Field> {
        override fun toString() = "${ref.type} $owner.$ref"
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
