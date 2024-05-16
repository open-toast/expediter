plugins {
    java
    id("com.toasttab.expediter") version "@VERSION@"
    id("com.toasttab.testkit.coverage") version "@TESTKIT_PLUGIN_VERSION@"
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
