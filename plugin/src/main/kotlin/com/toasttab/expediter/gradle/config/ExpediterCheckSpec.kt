package com.toasttab.expediter.gradle.config

import org.gradle.api.Action

class ExpediterCheckSpec {
    val application: ApplicationSpec = ApplicationSpec()
    val platform: PlatformSpec = PlatformSpec()

    val ignoreSpec = IgnoreSpec()

    var failOnIssues: Boolean = false

    fun application(configure: Action<ApplicationSpec>) {
        configure.execute(application)
    }

    fun platform(configure: Action<PlatformSpec>) {
        configure.execute(platform)
    }

    fun ignore(configure: Action<IgnoreSpec>) {
        configure.execute(ignoreSpec)
    }
}
