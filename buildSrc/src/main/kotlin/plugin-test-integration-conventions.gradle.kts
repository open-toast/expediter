plugins {
    id("maven-publish")
}

val repo = rootProject.layout.buildDirectory.dir("integration-repo").get().asFile.path

afterEvaluate {
    configurations.getAt("runtimeClasspath").incoming.artifactView {
        lenient(true)
        attributes.attribute(Attribute.of("artifactType", String::class.java), "jar")
    }.artifacts.map {
        it.id.componentIdentifier
    }.filterIsInstance<ProjectComponentIdentifier>().forEach {
        instrument(project(":${it.projectPath}"), repo)

        tasks.named("test") {
            dependsOn("${it.projectPath}:publishInstrumentedPublicationToIntegrationRepository")
        }
    }

    instrument(this, repo)
}

tasks.named("test") {
    dependsOn("publishInstrumentedPublicationToIntegrationRepository")
}

project.afterEvaluate {
    project.extensions.getByType(
        GradlePluginDevelopmentExtension::class.java
    ).plugins.forEach { plugin ->
        val name = "publish" + plugin.name.capitalize() + "PluginMarkerMavenPublicationToIntegrationRepository"

        tasks.named("test") {
            dependsOn(name)
        }
    }
}
