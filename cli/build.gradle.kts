plugins {
    `kotlin-conventions`
    `library-publishing-conventions`
    alias(libs.plugins.shadow)
}

tasks {
    shadowJar {
        manifest {
            attributes["Main-Class"] = "com.toasttab.expediter.cli.ExpediterCliCommandKt"
        }
    }
}

dependencies {
    implementation(libs.clikt)
    implementation(projects.core)
}
