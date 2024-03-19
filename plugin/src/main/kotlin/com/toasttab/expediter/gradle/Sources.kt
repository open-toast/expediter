package com.toasttab.expediter.gradle

import com.toasttab.model.types.ClassfileSource
import com.toasttab.model.types.ClassfileSourceType
import org.gradle.api.artifacts.ArtifactCollection
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.SourceSet
import java.io.File

fun ResolvedArtifactResult.source() =
    when (val cid = id.componentIdentifier) {
        is ModuleComponentIdentifier -> ClassfileSource(file, ClassfileSourceType.EXTERNAL_DEPENDENCY, cid.displayName)
        is ProjectComponentIdentifier -> ClassfileSource(file, ClassfileSourceType.SUBPROJECT_DEPENDENCY, cid.projectPath)
        else -> ClassfileSource(file, ClassfileSourceType.UNKNOWN, cid.displayName)
    }

fun File.source() = ClassfileSource(this, ClassfileSourceType.UNKNOWN, name)

fun SourceSet.sources() = output.map { ClassfileSource(it, ClassfileSourceType.SOURCE_SET, name) }

fun ArtifactCollection.sources() = artifacts.map { it.source() }

fun ConfigurableFileCollection.sources() = map { it.source() }

fun Collection<ArtifactCollection>.sources() = flatMapTo(LinkedHashSet(), ArtifactCollection::sources)

fun Set<SourceSet>.sources() = flatMap { it.sources() }
