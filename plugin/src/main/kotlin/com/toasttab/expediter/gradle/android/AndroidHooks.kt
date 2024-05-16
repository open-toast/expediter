package com.toasttab.expediter.gradle.android

import org.gradle.api.Project

fun Project.whenAndroidPluginApplied(f: Project.() -> Unit) {
    pluginManager.withPlugin("com.android.application") {
        f()
    }
    pluginManager.withPlugin("com.android.library") {
        f()
    }
}
