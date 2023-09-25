plugins {
    java
    id("com.toasttab.expediter")
    id("com.toasttab.testkit.coverage") version "0.0.2"
}

repositories {
    mavenCentral()
}

expediter {
    failOnIssues = true

    application {
        sourceSet("main")
    }

    platform {
        jvmVersion = 8
    }
}
