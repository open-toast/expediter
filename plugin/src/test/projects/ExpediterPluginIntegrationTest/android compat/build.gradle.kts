plugins {
    java
    id("com.toasttab.expediter")
    id("com.toasttab.testkit.coverage") version "0.0.4"
}

repositories {
    mavenCentral()
}

expediter {
    failOnIssues = true

    platform {
        androidSdk = 19
    }
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
}
