plugins {
    id("publishing-conventions")
}

publishing {
    publications {
        create<MavenPublication>("main") {
            from(components["java"])
        }
    }
}
