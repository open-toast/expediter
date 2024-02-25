plugins {
    `kotlin-conventions`
    `library-publishing-conventions`
    alias(libs.plugins.shadow)
}

tasks {
    shadowJar {
        manifest {
            attributes["Main-Class"] = "com.toasttab.expediter.cli.ExpediterCliCommandKt"
        }
    }

    test {
        systemProperty("classes", sourceSets.main.get().kotlin.classesDirectory.get().asFile.path)
        systemProperty("libraries", configurations.getAt("testRuntimeClasspath").asPath)
    }
}

dependencies {
    implementation(libs.clikt)
    implementation(libs.protobuf.java)
    implementation(projects.core)
}
