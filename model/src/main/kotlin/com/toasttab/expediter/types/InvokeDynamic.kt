package com.toasttab.expediter.types

data class InvokeDynamic(
    val ref: MemberSymbolicReference,
    val handle: InvokeDynamicHandle
)

data class InvokeDynamicHandle(
    val target: String,
    val ref: MemberSymbolicReference
)
