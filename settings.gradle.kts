enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        classpath("gradle.plugin.net.vivin:gradle-semantic-build-versioning:4.0.0")
    }
}


rootProject.name = "expediter"

apply(plugin = "net.vivin.gradle-semantic-build-versioning")

include(
    ":model",
    ":core",
    ":cli",
    ":plugin",
    ":tests",
    ":tests:lib1",
    ":tests:lib2",
    ":tests:base",
    ":animal-sniffer-format",
    ":proto"
)