package com.toasttab.expediter.gradle.config

open class AndroidApplicationSpec {
    val variants = mutableListOf<String>()
    var withRuntimeConfiguration = true

    fun variant(variant: String) {
        variants.add(variant)
    }

    fun withoutRuntimeConfiguration() {
        withRuntimeConfiguration = false
    }
}
