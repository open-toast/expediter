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
    implementation(libs.asm)
    api(projects.model)

    testImplementation(libs.junit)
    testImplementation(libs.strikt.core)
}
