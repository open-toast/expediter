package com.toasttab.expediter.roots

import com.toasttab.expediter.types.ApplicationType

interface RootsSelector {
    fun isRootType(type: ApplicationType): Boolean

    object All : RootsSelector {
        override fun isRootType(type: ApplicationType) = true
    }
}
