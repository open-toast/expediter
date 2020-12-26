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

import kotlinx.serialization.Serializable

sealed interface MemberSymbolicReference<M : MemberType> {
    val name: String
    val signature: String
    val type: M

    @Serializable
    data class FieldSymbolicReference(
        override val name: String,
        override val signature: String
    ) : MemberSymbolicReference<MemberType.Field> {
        override val type = MemberType.Field

        override fun toString() = "$name$signature"
    }

    @Serializable
    data class MethodSymbolicReference(
        override val name: String,
        override val signature: String
    ) : MemberSymbolicReference<MemberType.Method> {
        override val type = MemberType.Method

        fun isConstructor() = "<init>" == name

        override fun toString() = "$name$signature"
    }
}
