plugins {
    java
    id("com.toasttab.expediter")
}

repositories {
    mavenCentral()
}

expediter {
    failOnIssues = true

    application {
        sourceSet("main")
    }

    platform {
        platformClassloader = false
        animalSnifferConfiguration("sniffer")
    }
}

configurations.create("sniffer")

dependencies {
    add("sniffer", "com.toasttab.android:gummy-bears-api-19:0.5.1@signature")
}
