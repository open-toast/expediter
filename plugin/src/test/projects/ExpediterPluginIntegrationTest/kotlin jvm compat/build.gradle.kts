plugins {
    kotlin("jvm") version "@KOTLIN_VERSION@"
    id("com.toasttab.expediter")
    id("com.toasttab.testkit.coverage") version "@TESTKIT_PLUGIN_VERSION@"
}

repositories {
    mavenCentral()
}

expediter {
    failOnIssues = true

    platform {
        jvmVersion = 8
    }
}
