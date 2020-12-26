plugins {
    `kotlin-conventions`
    `library-publishing-conventions`
}

dependencies {
    implementation(libs.asm)
    api(projects.model)
}
