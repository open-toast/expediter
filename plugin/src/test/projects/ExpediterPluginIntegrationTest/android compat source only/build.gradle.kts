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
