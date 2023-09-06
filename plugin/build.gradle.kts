plugins {
    `kotlin-conventions`
    `kotlin-dsl`
    `plugin-publishing-conventions`
}

dependencies {
    implementation(gradleApi())
    implementation(projects.core)
    implementation(projects.animalSnifferFormat)

    testImplementation(gradleTestKit())
}

tasks {
    test {
        systemProperty("test-projects", layout.projectDirectory.dir("src/test/projects").asFile.path)

        // pipe the jacoco javaagent arguments into the new JVM that testkit launches
        // see TestProjectExtension and Gradle's JacocoPlugin class
        val jacoco = project.zipTree(configurations.getByName("jacocoAgent").asPath).filter {
            it.name == "jacocoagent.jar"
        }.singleFile
        val destfile = layout.buildDirectory.file("jacoco/test.exec").get()
        systemProperty("testkit-javaagent", "$jacoco=destfile=$destfile")
    }
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
