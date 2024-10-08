plugins {
    java
    id("com.toasttab.expediter") version "@VERSION@"
    id("com.toasttab.testkit.coverage") version "@TESTKIT_PLUGIN_VERSION@"
}

repositories {
    mavenCentral()
}

expediter {
    check("android") {
        platform {
            android {
                sdk = 21
                coreLibraryDesugaring = true
            }
        }
    }

    check("jvm") {
        platform {
            jvmVersion = 8
        }
    }
}
