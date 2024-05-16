import gradle.kotlin.dsl.accessors._76a779107637b25b34866585d88a55c4.publishing
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.testing.jacoco.plugins.JacocoPlugin

fun instrument(project: Project, repo: Any) {
    project.pluginManager.apply("maven-publish")

    project.tasks.register<InstrumentWithJacocoOfflineTask>("instrument") {
        dependsOn("jar")

        classpath = project.configurations.getAt(JacocoPlugin.ANT_CONFIGURATION_NAME)

        jar = project.tasks.named<Jar>("jar").flatMap { it.archiveFile }

        dir = project.layout.buildDirectory.dir("instrumented")
    }

    project.publishing {
        repositories {
            maven {
                name = "integration"
                url = project.uri("file://$repo")
            }
        }

        publications {
            create<MavenPublication>("instrumented") {
                from(project.components["java"])

                artifacts.clear()

                artifact(project.layout.buildDirectory.file("instrumented/${project.name}-${project.version}.jar")) {
                    builtBy(project.tasks.named("instrument"))
                }
            }
        }
    }

    project.tasks.named<GenerateModuleMetadata>("generateMetadataFileForInstrumentedPublication") {
        enabled = false
    }
}
