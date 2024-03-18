plugins {
    java
    id("com.toasttab.expediter")
    id("com.toasttab.testkit.coverage") version "@TESTKIT_PLUGIN_VERSION@"
}

repositories {
    mavenCentral()
}

expediter {
    failOnIssues = true

    platform {
        animalSnifferConfiguration("_animal_sniffer_descriptors_")
    }
}

configurations.create("_animal_sniffer_descriptors_")

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    add("_animal_sniffer_descriptors_", "com.toasttab.android:gummy-bears-api-21:0.8.0@signature")
}
