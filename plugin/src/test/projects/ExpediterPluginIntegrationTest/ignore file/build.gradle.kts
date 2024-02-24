plugins {
    java
    id("com.toasttab.expediter")
    id("com.toasttab.testkit.coverage") version "@TESTKIT_PLUGIN_VERSION@"
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

    platform {
        jvmVersion = 8
    }

    ignore {
        file = "expediter-ignore.json"
    }
}
