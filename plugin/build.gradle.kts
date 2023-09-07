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

        val destfile = layout.buildDirectory.file("jacoco/testkit.exec").get()
        // declare an additional jacoco output file so that the JUnit JVM and the TestKit JVM
        // do not try to write to the same file
        outputs.file(destfile).withPropertyName("testkit-jacoco")

        systemProperty("testkit-javaagent", "$jacoco=destfile=$destfile")

        // add the TestKit jacoco file to outgoing artifacts so that it can be aggregated
        configurations.getAt("coverageDataElementsForTest").outgoing.artifact(destfile) {
            type = ArtifactTypeDefinition.BINARY_DATA_TYPE
            builtBy("test")
        }
    }

    jacocoTestReport {
        // add the TestKit jacoco file to the local jacoco report
        this.executionData.from(layout.buildDirectory.dir("jacoco").map { files("test.exec", "testkit.exec") })
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
