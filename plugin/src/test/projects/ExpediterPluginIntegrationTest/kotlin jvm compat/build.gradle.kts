plugins {
    kotlin("jvm") version "@KOTLIN_VERSION@"
    id("com.toasttab.expediter")
    id("com.toasttab.testkit.coverage")
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
