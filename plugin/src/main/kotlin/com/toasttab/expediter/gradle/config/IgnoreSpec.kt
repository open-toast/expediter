package com.toasttab.expediter.gradle.config

import com.toasttab.expediter.ignore.Ignore

class IgnoreSpec {
    var file: Any? = null

    val ignores = mutableListOf<Ignore>()

    fun targetStartsWith(vararg partial: String) = or(Ignore.TargetStartsWith(*partial))

    fun callerStartsWith(vararg partial: String) = or(Ignore.CallerStartsWith(*partial))

    fun or(ignore: Ignore) {
        ignores.add(ignore)
    }

    fun buildIgnore() = if (ignores.isEmpty()) {
        Ignore.NOTHING
    } else if (ignores.size == 1) {
        ignores[0]
    } else {
        Ignore.Or(*ignores.toTypedArray())
    }
}
