package com.toasttab.expediter

import com.toasttab.expediter.types.AccessProtection
import com.toasttab.expediter.types.MemberDescriptor
import com.toasttab.expediter.types.MemberType
import com.toasttab.expediter.types.TypeDescriptor

object AccessCheck {
    fun <M : MemberType> allowedAccess(caller: TypeDescriptor, owner: TypeDescriptor, member: MemberDescriptor<M>): Boolean {
        return if (member.protection == AccessProtection.PRIVATE) {
            return caller.sameClassAs(owner)
        } else if (member.protection == AccessProtection.PACKAGE_PRIVATE || owner.protection == AccessProtection.PACKAGE_PRIVATE) {
            return caller.samePackageAs(owner)
        } else {
            true
        } // TODO: add other checks
    }

    private fun TypeDescriptor.sameClassAs(other: TypeDescriptor) = name == other.name
    private fun TypeDescriptor.samePackageAs(other: TypeDescriptor) = name.substringBeforeLast('/') == other.name.substringBeforeLast('/')
}