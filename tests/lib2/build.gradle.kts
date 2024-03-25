plugins {
    `kotlin-conventions`
    `java-test-fixtures`
}

dependencies {
    testFixturesApi(testFixtures(projects.tests.base))
}
