plugins {
    java
    id("com.toasttab.expediter")
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
