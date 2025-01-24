plugins {
    java
    id("com.toasttab.expediter")
    id("com.toasttab.testkit.coverage")
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
