package com.toasttab.expediter.roots

import com.toasttab.expediter.types.ApplicationType

interface RootSelector {
    fun isRootType(type: ApplicationType): Boolean

    object All: RootSelector {
        override fun isRootType(type: ApplicationType) = true
    }
}