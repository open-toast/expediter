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
