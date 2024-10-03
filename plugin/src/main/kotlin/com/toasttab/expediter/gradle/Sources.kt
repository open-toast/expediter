package com.toasttab.expediter.gradle

import com.toasttab.expediter.types.ClassfileSource
import com.toasttab.expediter.types.ClassfileSourceType
import org.gradle.api.artifacts.ArtifactCollection
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.provider.ListProperty
import java.io.File

fun ResolvedArtifactResult.source() =
    when (id.componentIdentifier) {
        is ModuleComponentIdentifier -> ClassfileSource(file, ClassfileSourceType.EXTERNAL_DEPENDENCY)
        is ProjectComponentIdentifier -> ClassfileSource(file, ClassfileSourceType.SUBPROJECT_DEPENDENCY)
        else -> ClassfileSource(file, ClassfileSourceType.UNKNOWN)
    }

fun File.source() = ClassfileSource(this, ClassfileSourceType.UNKNOWN)

fun ArtifactCollection.sources() = artifacts.map { it.source() }

fun ConfigurableFileCollection.sources() = map { it.source() }

fun Collection<ArtifactCollection>.sources() = flatMapTo(LinkedHashSet(), ArtifactCollection::sources)

fun ListProperty<out FileSystemLocation>.sources() = get().map { ClassfileSource(it.asFile, ClassfileSourceType.PROJECT) }
