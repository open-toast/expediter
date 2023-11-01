package com.toasttab.expediter

import com.toasttab.expediter.types.ApplicationTypeWithResolvedHierarchy
import com.toasttab.expediter.types.ResolvedTypeHierarchy
import protokt.v1.toasttab.expediter.v1.AccessProtection
import protokt.v1.toasttab.expediter.v1.MemberDescriptor
import protokt.v1.toasttab.expediter.v1.TypeDescriptor

object AccessCheck {
    fun allowedAccess(caller: ApplicationTypeWithResolvedHierarchy, target: TypeDescriptor, member: MemberDescriptor): Boolean {
        return if (member.protection == AccessProtection.PRIVATE) {
            caller.name == target.name
        } else if (member.protection == AccessProtection.PACKAGE_PRIVATE || target.protection == AccessProtection.PACKAGE_PRIVATE) {
            samePackage(caller.name, target.name)
        } else if (member.protection == AccessProtection.PROTECTED) {
            samePackage(caller.name, target.name) ||
                // if we can't resolve all supertypes, we can't say for sure that access is not allowed
                caller.hierarchy !is ResolvedTypeHierarchy.CompleteTypeHierarchy ||
                caller.hierarchy.allTypes.any { it.name == target.name }
        } else {
            true
        }
    }
    private fun samePackage(a: String, b: String) = a.substringBeforeLast('/') == b.substringBeforeLast('/')
}
