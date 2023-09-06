plugins {
    `kotlin-conventions`
}

configurations.create("lib2") {
    isTransitive = false
}

tasks {
    test {
        val files = configurations.getByName("lib2").files.map { it.path } + sourceSets.main.get().java.classesDirectory.get().asFile.path

        systemProperty("test-classpath", files.joinToString(separator = ":"))

        dependsOn("lib2:jar")
    }
}

dependencies {
    implementation(projects.tests.lib1)

    add("lib2", projects.tests.lib2)

    testImplementation(projects.core)
}
