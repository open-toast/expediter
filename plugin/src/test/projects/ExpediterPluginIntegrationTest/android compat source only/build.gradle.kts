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

    application {
        sourceSet("main")
    }

    platform {
        androidSdk = 19

        configuration("runtimeClasspath")
    }
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
}
