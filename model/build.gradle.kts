plugins {
    `kotlin-conventions`
    `library-publishing-conventions`
    alias(libs.plugins.serialization)
}

dependencies {
    implementation(libs.serialization.json)
    api(projects.proto)
}