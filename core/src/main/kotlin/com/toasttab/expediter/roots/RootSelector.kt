package com.toasttab.expediter.roots

import com.toasttab.expediter.types.ApplicationType
import com.toasttab.expediter.types.ClassfileSourceType

interface RootSelector {
    fun isRoot(type: ApplicationType): Boolean

    object All : RootSelector {
        override fun isRoot(type: ApplicationType) = true
    }

    object ProjectClasses : RootSelector {
        override fun isRoot(type: ApplicationType) = type.source.type == ClassfileSourceType.SOURCE_SET || type.source.type == ClassfileSourceType.SUBPROJECT_DEPENDENCY
    }
}
