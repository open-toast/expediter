package com.toasttab.expediter.gradle.config

import com.toasttab.expediter.ignore.Ignore
import org.slf4j.LoggerFactory

class IgnoreSpec {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(PlatformSpec::class.java)
    }

    private var file: Any? = null
        set(value) {
            LOGGER.warn("file property is deprecated, use the file function instead: ignore { file('path') }")
            value?.let(files::add)
        }

    val files = mutableListOf<Any>()

    val ignores = mutableListOf<Ignore>()

    fun file(file: Any) {
        files.add(file)
    }

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
