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
        androidSdk = 19
    }
}
