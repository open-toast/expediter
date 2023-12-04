package com.toasttab.expediter.gradle.config

class AndroidSpec {
    var sdk: Int? = null
    var coreLibraryDesugaring: Boolean = false
    var gummyBearsVersion: String = "0.8.0"

    fun artifact(): String {
        val base = "com.toasttab.android:gummy-bears-api-$sdk:$gummyBearsVersion"

        return if (coreLibraryDesugaring) {
            "$base:coreLib2@expediter"
        } else {
            "$base@expediter"
        }
    }
}
