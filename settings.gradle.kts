enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "expediter"

include(
    ":model",
    ":core",
    ":plugin",
    ":tests",
    ":tests:lib1",
    ":tests:lib2",
    ":tests:base",
    ":animal-sniffer-format"
)