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

/**
 * This object enumerates signature-polymorphic methods, see the javadoc for MethodHandle.
 */
object PolymorphicMethods {
    private val methods = mapOf(
        "java/lang/invoke/MethodHandle" to setOf(
            "invoke",
            "invokeExact"
        ),

        "java/lang/invoke/VarHandle" to setOf(
            "get",
            "set",
            "getVolatile",
            "setVolatile",
            "getOpaque",
            "setOpaque",
            "getAcquire",
            "setRelease",
            "compareAndSet",
            "compareAndExchange",
            "compareAndExchangeAcquire",
            "compareAndExchangeRelease",
            "weakCompareAndSetPlain",
            "weakCompareAndSet",
            "weakCompareAndSetAcquire",
            "weakCompareAndSetRelease",
            "getAndSet",
            "getAndSetAcquire",
            "getAndSetRelease",
            "getAndAdd",
            "getAndAddAcquire",
            "getAndAddRelease",
            "getAndBitwiseOr",
            "getAndBitwiseOrAcquire",
            "getAndBitwiseOrRelease",
            "getAndBitwiseAnd",
            "getAndBitwiseAndAcquire",
            "getAndBitwiseAndRelease",
            "getAndBitwiseXor",
            "getAndBitwiseXorAcquire",
            "getAndBitwiseXorRelease"
        )
    )

    fun contains(className: String, methodName: String): Boolean {
        return methods[className]?.contains(methodName) ?: false
    }
}
