plugins {
    java
    id("com.toasttab.expediter") version "@VERSION@"
}

expediter {
    failOnIssues = true

    platform {
        jvmVersion = 8
    }
}

dependencies {
    implementation(project(":lib"))
}
