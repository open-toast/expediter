plugins {
    kotlin("jvm") version "@KOTLIN_VERSION@"
    id("com.toasttab.protokt") version "@PROTOKT_VERSION@"
    id("com.toasttab.expediter")
    id("com.toasttab.testkit.coverage") version "@TESTKIT_PLUGIN_VERSION@"
}

dependencies {
    implementation("com.google.protobuf:protobuf-java:@PROTOBUF_VERSION@")
}

repositories {
    mavenCentral()
}

expediter {
    failOnIssues = true

    platform {
        android {
            sdk = 21
        }
    }
}
