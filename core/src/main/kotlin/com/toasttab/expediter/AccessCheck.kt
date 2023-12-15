package com.toasttab.expediter

import com.toasttab.expediter.types.ResolvedTypeHierarchy
import com.toasttab.expediter.types.Type
import protokt.v1.toasttab.expediter.v1.AccessDeclaration
import protokt.v1.toasttab.expediter.v1.AccessProtection
import protokt.v1.toasttab.expediter.v1.MemberDescriptor
import protokt.v1.toasttab.expediter.v1.TypeDescriptor

object AccessCheck {
    /**
     * Check if a class is allowed to access a member of another class.
     *
     * This check is lenient. If there's insufficient information to determine access, this method returns true.
     * E.g. if member protection is unknown, we assume it's public, and access is allowed. Similarly,
     * for protected members, if the hierarchy of the application type cannot be resolved, we assume access is allowed.
     *
     * See JVM Specification, 5.4.4 "Access Control" and `sun.invoke.util.VerifyAccess.isMemberAccessible`.
     *
     * @param caller the class trying to access the member
     * @param refType the type through which the member is referenced (i.e. the class from the symbolic reference in the bytecode).
     * @param member the member being accessed
     * @param declaringType the type that declares the member
     */
    fun allowedAccess(
        caller: ResolvedTypeHierarchy,
        refType: ResolvedTypeHierarchy.CompleteTypeHierarchy,
        member: MemberDescriptor,
        declaringType: Type
    ): Boolean {
        // reference type must be accessible from the caller
        if (!isClassAccessible(caller, refType.type.descriptor)) {
            return false
        }

        return when (member.protection) {
            // public members are accessible
            is AccessProtection.UNRECOGNIZED, AccessProtection.UNKNOWN, AccessProtection.PUBLIC -> true
            // package private members are accessible to classes in the same package as the declaring type
            AccessProtection.PACKAGE_PRIVATE -> samePackage(caller.name, declaringType.name)
            // private members are accessible to classes in the same nest as the declaring type
            AccessProtection.PRIVATE -> sameNest(caller.name, declaringType.name)
            // protected logic is more complicated, see below
            AccessProtection.PROTECTED -> isProtectedAccessAllowed(caller, refType, member, declaringType.descriptor)
        }
    }

    private fun isProtectedAccessAllowed(
        caller: ResolvedTypeHierarchy,
        refType: ResolvedTypeHierarchy.CompleteTypeHierarchy,
        member: MemberDescriptor,
        declaringType: TypeDescriptor
    ): Boolean {
        // protected members can be accessed from classes in the same package as the declaring type
        if (samePackage(caller.name, declaringType.name)) {
            return true
        }

        // if the caller type hierarchy cannot be resolved, we cannot proceed further, so assume member is accessible
        if (caller !is ResolvedTypeHierarchy.CompleteTypeHierarchy) {
            return true
        }

        // if the caller is not in the same package as the declaring type, it must inherit from the declaring type
        return if (caller.isSubtypeOf(declaringType)) {
            // if the member is not static,
            // the caller, the reference type, and the declaring type must be part of the same hierarchy,
            // i.e. the caller must be a subtype or a supertype of the reference type
            member.declaration != AccessDeclaration.INSTANCE ||
                refType.isSubtypeOf(caller.type.descriptor) ||
                caller.isSubtypeOf(refType.type.descriptor)
        } else {
            false
        }
    }

    private fun ResolvedTypeHierarchy.CompleteTypeHierarchy.isSubtypeOf(type: TypeDescriptor): Boolean {
        return allTypes.any { it.name == type.name }
    }

    private fun isClassAccessible(caller: ResolvedTypeHierarchy, target: TypeDescriptor): Boolean {
        return when (target.protection) {
            // public classes are accessible
            is AccessProtection.UNRECOGNIZED, AccessProtection.UNKNOWN, AccessProtection.PUBLIC -> true
            // package-private classes are accessible from classes in the same package
            // (note: inner classes can be declared private but are still package-private in the bytecode)
            else -> samePackage(caller.name, target.name)
        }
    }

    private fun samePackage(a: String, b: String) = a.substringBeforeLast('/') == b.substringBeforeLast('/')

    private fun sameNest(a: String, b: String) = a.substringBeforeLast('$') == b.substringBeforeLast('$')
}
