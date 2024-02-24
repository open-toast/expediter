package com.toasttab.expediter.roots

import com.toasttab.expediter.types.ApplicationType

interface RootSelector {
    fun isRoot(type: ApplicationType): Boolean

    object All : RootSelector {
        override fun isRoot(type: ApplicationType) = true
    }
}
