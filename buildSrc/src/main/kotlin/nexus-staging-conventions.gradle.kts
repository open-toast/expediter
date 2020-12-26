import org.gradle.kotlin.dsl.configure

plugins {
    id("io.github.gradle-nexus.publish-plugin")
}

if (isRelease()) {
    nexusPublishing {
        repositories {
            sonatype {
                username = Remote.username
                password = Remote.password
            }
        }
    }
}
