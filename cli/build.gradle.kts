plugins {
    `kotlin-conventions`
    `library-publishing-conventions`
    alias(libs.plugins.shadow)
}

val ANDROID_LIB = "_android_lib_"
val ANDROID_DESCRIPTORS = "_android_descriptors_"

tasks {
    shadowJar {
        manifest {
            attributes["Main-Class"] = "com.toasttab.expediter.cli.ExpediterCliCommandKt"
        }
    }

    test {
        systemProperty("classes", sourceSets.main.get().kotlin.classesDirectory.get().asFile.path)
        systemProperty("libraries", configurations.getAt("testRuntimeClasspath").asPath)
        systemProperty("android-libraries", configurations.getAt(ANDROID_LIB).asPath)
        systemProperty("android-descriptors", configurations.getAt(ANDROID_DESCRIPTORS).asPath)
    }
}

configurations.create(ANDROID_LIB).isTransitive = false
configurations.create(ANDROID_DESCRIPTORS).isTransitive = false

dependencies {
    implementation(libs.clikt)
    implementation(libs.protobuf.java)
    implementation(projects.core)

    addProvider<MinimalExternalModuleDependency, ExternalModuleDependency>(ANDROID_DESCRIPTORS, libs.gummy.bears) {
        artifact {
            extension = "expediter"
        }
    }
    add(ANDROID_LIB, libs.timber)
}
