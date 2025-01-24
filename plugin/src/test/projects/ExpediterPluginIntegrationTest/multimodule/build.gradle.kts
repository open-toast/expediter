plugins {
    id("com.toasttab.testkit.coverage")
    id("com.toasttab.expediter") apply false
}

subprojects {
    repositories {
        mavenCentral()
    }
}