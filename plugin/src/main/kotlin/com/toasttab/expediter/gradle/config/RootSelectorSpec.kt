package com.toasttab.expediter.gradle.config

import com.toasttab.expediter.roots.RootSelector
import com.toasttab.expediter.types.ApplicationType
import com.toasttab.expediter.types.ClassfileSourceType

open class RootSelectorSpec {
    var type: RootType = RootType.PROJECT_CLASSES

    fun all() {
        type = RootType.ALL
    }

    fun project() {
        type = RootType.PROJECT_CLASSES
    }
}

enum class RootType(val selector: RootSelector) {
    ALL(RootSelector.All),
    PROJECT_CLASSES(object : RootSelector {
        override fun isRoot(type: ApplicationType) = type.source.type == ClassfileSourceType.SOURCE_SET || type.source.type == ClassfileSourceType.SUBPROJECT_DEPENDENCY
    })
}
