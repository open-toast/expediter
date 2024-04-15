plugins {
    `kotlin-conventions`
    `kotlin-dsl`
    `plugin-publishing-conventions`
    alias(libs.plugins.testkit.plugin)
    alias(libs.plugins.build.config)
}

dependencies {
    implementation(projects.core)
    implementation(projects.animalSnifferFormat)
    implementation(libs.protobuf.java)

   testImplementation(libs.testkit.junit5)
}

testkitTests {
    replaceToken("KOTLIN_VERSION", libs.versions.kotlin.get())
    replaceToken("PROTOKT_VERSION", libs.versions.protokt.get())
    replaceToken("PROTOBUF_VERSION", libs.versions.protobuf.get())
}

buildConfig {
    packageName.set("com.toasttab.expediter.gradle")
    buildConfigField("String", "GUMMY_BEARS_VERSION", "\"${libs.versions.gummy.bears.get()}\"")
}

gradlePlugin {
    plugins {
        create("expediter") {
            id = "com.toasttab.expediter"
            implementationClass = "com.toasttab.expediter.gradle.ExpediterPlugin"
            description = ProjectInfo.description
            displayName = ProjectInfo.name
            tags = listOf("abi", "compatibility")
        }
    }
}
