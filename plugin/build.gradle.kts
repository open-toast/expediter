plugins {
    `kotlin-conventions`
    `kotlin-dsl`
    `plugin-publishing-conventions`
    alias(libs.plugins.testkit.plugin)
}

dependencies {
    implementation(projects.core)
    implementation(projects.animalSnifferFormat)

   testImplementation(libs.testkit.extension)
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
