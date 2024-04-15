package com.toasttab.expediter.gradle.config

import com.toasttab.expediter.gradle.BuildConfig

open class AndroidSpec {
    var sdk: Int? = null
    var coreLibraryDesugaring: Boolean = false
    var gummyBearsVersion: String = BuildConfig.GUMMY_BEARS_VERSION

    fun artifact(): String {
        val base = "com.toasttab.android:gummy-bears-api-$sdk:$gummyBearsVersion"

        return if (coreLibraryDesugaring) {
            "$base:coreLib2@expediter"
        } else {
            "$base@expediter"
        }
    }
}
