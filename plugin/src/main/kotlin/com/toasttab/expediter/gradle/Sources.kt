package com.toasttab.expediter.gradle

import com.toasttab.expediter.types.ClassfileSource
import com.toasttab.expediter.types.ClassfileSourceType
import org.gradle.api.artifacts.ArtifactCollection
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.attributes.AttributeContainer
import org.gradle.api.capabilities.Capability
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.internal.artifacts.configurations.ArtifactCollectionInternal
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.artifact.ArtifactVisitor
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.artifact.ResolvableArtifact
import org.gradle.api.internal.attributes.ImmutableAttributes
import org.gradle.api.provider.ListProperty
import org.gradle.internal.DisplayName
import org.gradle.internal.component.external.model.ImmutableCapabilities
import java.io.File

fun File.source() = ClassfileSource(this, ClassfileSourceType.UNKNOWN)

fun ConfigurableFileCollection.sources() = map { it.source() }

fun ResolvableArtifact.source() = when (id.componentIdentifier) {
    is ModuleComponentIdentifier -> ClassfileSource(file, ClassfileSourceType.EXTERNAL_DEPENDENCY)
    is ProjectComponentIdentifier -> ClassfileSource(file, ClassfileSourceType.SUBPROJECT_DEPENDENCY)
    else -> ClassfileSource(file, ClassfileSourceType.UNKNOWN)
}

// Note that ArtifactCollection.artifactFiles coalesces multiple files associated
// with the same artifact; for example, when a project dependency has multiple outputs
// (e.g. build/classes/kotlin/main and build/classes/java/main), only one of those
// outputs will be present in ArtifactCollection.artifactFiles.
//
// To deal with that, we visit all artifacts, similarly to the implementation
// of ArtifactCollection.artifactFiles, but we don't coalesce the files.
//
// The ArtifactVisitor#visitArtifact API is internal and differs across Gradle versions,
// so we have to "override" multiple versions of this method.
class ArtifactCollectingVisitor : ArtifactVisitor {
    private val sources = mutableListOf<ClassfileSource>()

    // Gradle >= 8.14
    override fun visitArtifact(
        variantName: DisplayName,
        variantAttributes: ImmutableAttributes,
        capabilities: ImmutableCapabilities,
        artifact: ResolvableArtifact
    ) {
        sources.add(artifact.source())
    }

    // Gradle >= 8.6 < 8.14
    @Suppress("unused", "unused_parameter")
    fun visitArtifact(
        variantName: DisplayName,
        variantAttributes: AttributeContainer,
        capabilities: ImmutableCapabilities,
        artifact: ResolvableArtifact
    ) {
        sources.add(artifact.source())
    }

    // Gradle < 8.6
    @Suppress("unused", "unused_parameter")
    fun visitArtifact(
        variantName: DisplayName,
        variantAttributes: AttributeContainer,
        capabilities: List<Capability>,
        artifact: ResolvableArtifact
    ) {
        sources.add(artifact.source())
    }

    override fun requireArtifactFiles() = true

    override fun visitFailure(failure: Throwable) {
    }

    fun sources() = sources
}

private fun ArtifactCollectionInternal.sources(): Collection<ClassfileSource> {
    val visitor = ArtifactCollectingVisitor()
    visitArtifacts(visitor)
    return visitor.sources()
}

fun Collection<ArtifactCollection>.sources() = flatMapTo(LinkedHashSet()) {
    (it as ArtifactCollectionInternal).sources()
}

fun ListProperty<out FileSystemLocation>.sources() = get().map { ClassfileSource(it.asFile, ClassfileSourceType.PROJECT) }
