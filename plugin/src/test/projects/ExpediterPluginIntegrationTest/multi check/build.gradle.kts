plugins {
    java
    id("com.toasttab.expediter")
    id("com.toasttab.testkit.coverage") version "0.0.4"
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
