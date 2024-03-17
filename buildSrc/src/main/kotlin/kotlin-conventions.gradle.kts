import org.gradle.api.JavaVersion
import org.gradle.kotlin.dsl.repositories
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

repositories {
    mavenCentral()
}

plugins {
    kotlin("jvm")
    `jvm-test-suite`
    jacoco
    id("com.diffplug.spotless")
}

spotless {
    kotlin {
        ktlint(libs.versions.ktlint.get())
        target("src/**/*.kt")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
        languageVersion = "1.6"
        apiVersion = "1.6"
        freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
    }
}

tasks {
    test {
        useJUnitPlatform()

        extensions.configure<JacocoTaskExtension> {
            includes = listOf("com.toasttab.expediter.*")
        }
    }
}

dependencies {
    testImplementation(libs.junit)
    testImplementation(libs.strikt.core)
}
