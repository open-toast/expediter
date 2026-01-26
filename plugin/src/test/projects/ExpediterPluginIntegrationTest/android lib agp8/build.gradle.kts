plugins {
    id("com.android.library") version "8.4.1"
    id("org.jetbrains.kotlin.android") version "@KOTLIN_VERSION@"
    id("com.toasttab.expediter")
    id("com.toasttab.testkit.coverage")
}

expediter {
    failOnIssues = true

    application {
        android {
            variant("debug")
            variant("release")
        }
    }

    platform {
        android {
            sdk = 21
        }
    }
}

android {
   namespace = "test.lib"
    compileSdk = 33

    defaultConfig {
        minSdk = 33
        targetSdk = 33

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}
