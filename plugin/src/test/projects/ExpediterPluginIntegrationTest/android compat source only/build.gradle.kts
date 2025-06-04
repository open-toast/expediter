plugins {
    java
    id("com.toasttab.expediter")
    id("com.toasttab.testkit.coverage")
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
        android {
            sdk = 19
        }

        configuration("runtimeClasspath")
    }
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
}
