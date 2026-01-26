plugins {
    id("io.github.gradle-nexus.publish-plugin")
}

if (isRelease()) {
    nexusPublishing {
        repositories {
            sonatype {
                nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
                username = Remote.username
                password = Remote.password
            }
        }
    }
}
