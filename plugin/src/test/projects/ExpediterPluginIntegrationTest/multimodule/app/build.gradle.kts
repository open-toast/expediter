plugins {
    java
    id("com.toasttab.expediter")
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
