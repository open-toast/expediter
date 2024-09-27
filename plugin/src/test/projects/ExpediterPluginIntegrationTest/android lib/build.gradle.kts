// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.library") version "8.4.1"
    id("org.jetbrains.kotlin.android") version "@KOTLIN_VERSION@"
    id("com.toasttab.expediter") version "@VERSION@"
    id("com.toasttab.testkit.coverage") version "@TESTKIT_PLUGIN_VERSION@"
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
        androidSdk = 21
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
