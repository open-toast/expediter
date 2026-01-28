import org.gradle.api.JavaVersion
import org.gradle.kotlin.dsl.repositories
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

repositories {
    mavenCentral()
    google()
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

jacoco {
    toolVersion = "0.8.14"
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
        languageVersion = KotlinVersion.KOTLIN_1_8
        apiVersion = KotlinVersion.KOTLIN_1_8
        freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
    }
}

tasks {
    test {
        useJUnitPlatform()

        extensions.configure<JacocoTaskExtension> {
            includes = listOf("com.toasttab.expediter.*", "protokt.v1.toasttab.expediter.*")
        }
    }
}

dependencies {
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.launcher)
    testImplementation(libs.strikt.core)
}
