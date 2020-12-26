plugins {
    `kotlin-conventions`
    `library-publishing-conventions`
}

tasks {
    test {
        useJUnitPlatform()
    }
}

dependencies {
    api(projects.model)

    testImplementation(libs.junit)
    testImplementation(libs.strikt.core)
}