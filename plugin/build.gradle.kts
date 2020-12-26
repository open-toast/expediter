plugins {
    `kotlin-conventions`
    `kotlin-dsl`
    `plugin-publishing-conventions`
}

dependencies {
    implementation(gradleApi())
    implementation(projects.core)
    implementation(projects.animalSnifferFormat)

    testImplementation(libs.junit)
    testImplementation(libs.strikt.core)
    testImplementation(gradleTestKit())
}

tasks {
    test {
        useJUnitPlatform()
        systemProperty("test-projects", layout.projectDirectory.dir("src/test/projects").asFile.path)
    }
}

gradlePlugin {
    plugins {
        create("expediter") {
            id = "com.toasttab.expediter"
            implementationClass = "com.toasttab.expediter.gradle.ExpediterPlugin"
            description = ProjectInfo.description
            displayName = ProjectInfo.name
        }
    }
}
