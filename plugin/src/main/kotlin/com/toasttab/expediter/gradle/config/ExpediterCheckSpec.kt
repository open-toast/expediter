package com.toasttab.expediter.gradle.config

import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject

open class ExpediterCheckSpec @Inject constructor(
    objectFactory: ObjectFactory
) {
    val application: ApplicationSpec = objectFactory.newInstance()
    val platform: PlatformSpec = objectFactory.newInstance()
    val ignoreSpec: IgnoreSpec = objectFactory.newInstance()

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
