plugins {
    `kotlin-conventions`
    `java-test-fixtures`
}

configurations.create("lib2") {
    isTransitive = false
}

tasks {
    test {
        val files = configurations.getByName("lib2").files.map { it.path } + sourceSets.testFixtures.get().java.classesDirectory.get().asFile.path

        systemProperty("test-classpath", files.joinToString(separator = ":"))

        dependsOn("lib2:testFixturesJar")
    }
}

dependencies {
    testFixturesImplementation(testFixtures(projects.tests.lib1))

    add("lib2", testFixtures(projects.tests.lib2))

    testImplementation(projects.core)
}
