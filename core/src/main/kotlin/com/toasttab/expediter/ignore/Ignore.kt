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

package com.toasttab.expediter.ignore

import com.toasttab.expediter.types.MemberSymbolicReference
import java.io.Serializable

interface Ignore : Serializable {
    fun ignore(caller: String?, type: String?, ref: MemberSymbolicReference<*>?): Boolean

    companion object {
        val NOTHING: Ignore = object : Ignore {
            override fun ignore(caller: String?, type: String?, ref: MemberSymbolicReference<*>?) = false
        }
    }

    class Not(
        private val ignore: Ignore
    ) : Ignore {
        override fun ignore(caller: String?, type: String?, ref: MemberSymbolicReference<*>?) = !ignore.ignore(caller, type, ref)
    }

    class And(
        private vararg val ignores: Ignore
    ) : Ignore {
        override fun ignore(caller: String?, type: String?, ref: MemberSymbolicReference<*>?) = ignores.all { it.ignore(caller, type, ref) }
    }

    class Or(
        private vararg val ignores: Ignore
    ) : Ignore {
        override fun ignore(caller: String?, type: String?, ref: MemberSymbolicReference<*>?) = ignores.any { it.ignore(caller, type, ref) }
    }

    object IsConstructor : Ignore {
        override fun ignore(caller: String?, type: String?, ref: MemberSymbolicReference<*>?) = ref is MemberSymbolicReference.MethodSymbolicReference && ref.isConstructor()
    }

    object Caller {
        class StartsWith(
            private vararg val partial: String
        ) : Ignore {
            override fun ignore(caller: String?, type: String?, ref: MemberSymbolicReference<*>?) = caller != null && partial.any { caller.startsWith(it) }
        }
    }

    object Type {
        class StartsWith(
            private vararg val partial: String
        ) : Ignore {
            override fun ignore(caller: String?, type: String?, ref: MemberSymbolicReference<*>?) = partial.any { type != null && type.startsWith(it) }
        }
    }

    class Signature(
        private val signature: String
    ) : Ignore {
        override fun ignore(caller: String?, type: String?, ref: MemberSymbolicReference<*>?) = ref?.signature == signature

        companion object {
            val IS_BLANK = Signature("()V")
        }
    }
}
infix fun Ignore.and(other: Ignore): Ignore = Ignore.And(this, other)
infix fun Ignore.or(other: Ignore): Ignore = Ignore.Or(this, other)
