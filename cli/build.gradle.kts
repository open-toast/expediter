import org.jetbrains.kotlin.gradle.utils.`is`

plugins {
    `kotlin-conventions`
    `library-publishing-conventions`
    alias(libs.plugins.shadow)
}

val ANDROID = "_android_lib_"

tasks {
    shadowJar {
        manifest {
            attributes["Main-Class"] = "com.toasttab.expediter.cli.ExpediterCliCommandKt"
        }
    }

    test {
        systemProperty("classes", sourceSets.main.get().kotlin.classesDirectory.get().asFile.path)
        systemProperty("libraries", configurations.getAt("testRuntimeClasspath").asPath)
        systemProperty("android-libraries", configurations.getAt(ANDROID).asPath)
    }
}

configurations.create(ANDROID).isTransitive = false

dependencies {
    implementation(libs.clikt)
    implementation(libs.protobuf.java)
    implementation(projects.core)

    add(ANDROID, libs.timber)
}
