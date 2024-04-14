// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.library") version "8.1.2"
    id("org.jetbrains.kotlin.android") version "@KOTLIN_VERSION@"
    id("com.toasttab.expediter")
    id("com.toasttab.testkit.coverage")
}

expediter {
    failOnIssues = true

    application {
        roots {
            all()
        }

        configuration("releaseRuntimeClasspath")
        configuration("debugRuntimeClasspath")
    }

    platform {
        androidSdk = 21
    }
}

android {
   namespace = "test.lib"
    compileSdk = 33

    defaultConfig {
        minSdk = 21
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

