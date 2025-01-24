plugins {
    java
    id("com.toasttab.expediter")
    id("com.toasttab.testkit.coverage")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    implementation("com.fasterxml.jackson.core:jackson-core") {
        version {
            strictly("2.13.5")
        }
    }
}

expediter {
    failOnIssues = true

    application {
        roots {
            all()
        }
    }

    platform {
        jvmVersion = 8
    }
}
