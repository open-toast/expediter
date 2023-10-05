package com.toasttab.expediter

import com.toasttab.expediter.types.AccessProtection
import com.toasttab.expediter.types.ApplicationTypeWithResolvedHierarchy
import com.toasttab.expediter.types.MemberDescriptor
import com.toasttab.expediter.types.MemberType
import com.toasttab.expediter.types.ResolvedOptionalTypeHierarchy
import com.toasttab.expediter.types.ResolvedTypeHierarchy
import com.toasttab.expediter.types.TypeDescriptor

object AccessCheck {
    fun <M : MemberType> allowedAccess(caller: ApplicationTypeWithResolvedHierarchy, target: TypeDescriptor, member: MemberDescriptor<M>): Boolean {
        return if (member.protection == AccessProtection.PRIVATE) {
            caller.name == target.name
        } else if (member.protection == AccessProtection.PACKAGE_PRIVATE || target.protection == AccessProtection.PACKAGE_PRIVATE) {
            samePackage(caller.name, target.name)
        } else if (member.protection == AccessProtection.PROTECTED) {
            samePackage(caller.name, target.name)
                    || caller.hierarchy !is ResolvedTypeHierarchy.CompleteTypeHierarchy
                    || caller.hierarchy.allTypes.any { it.name == target.name }
        } else {
            true
        }
    }
    private fun samePackage(a: String, b: String) = a.substringBeforeLast('/') == b.substringBeforeLast('/')
}
