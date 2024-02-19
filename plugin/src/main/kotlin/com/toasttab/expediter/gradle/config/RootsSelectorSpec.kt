package com.toasttab.expediter.gradle.config

import com.toasttab.expediter.roots.RootsSelector
import com.toasttab.expediter.types.ApplicationType
import com.toasttab.expediter.types.ClassfileSourceType

open class RootsSelectorSpec {
    var type: RootType = RootType.PROJECT_CLASSES

    fun all() {
        type = RootType.ALL
    }

    fun project() {
        type = RootType.PROJECT_CLASSES
    }
}

enum class RootType(val selector: RootsSelector) {
    ALL(RootsSelector.All),
    PROJECT_CLASSES(object : RootsSelector {
        override fun isRootType(type: ApplicationType) = type.source.type == ClassfileSourceType.SOURCE_SET || type.source.type == ClassfileSourceType.SUBPROJECT_DEPENDENCY
    })
}
