package com.toasttab.expediter.gradle.config

class RootSpec(
    var type: RootType = RootType.ALL
) {
    fun project() {
        type = RootType.PROJECT_CLASSES
    }
}

enum class RootType {
    ALL,
    PROJECT_CLASSES
}