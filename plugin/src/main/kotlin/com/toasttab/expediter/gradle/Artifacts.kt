package com.toasttab.expediter.gradle

import com.toasttab.expediter.types.SourceType
import com.toasttab.expediter.types.TypeSource
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.file.SourceDirectorySet
import java.io.File

fun ResolvedArtifactResult.source() =
    when (val cid = id.componentIdentifier) {
        is ModuleComponentIdentifier -> TypeSource(file, SourceType.DEPENDENCY, cid.displayName)
        is ProjectComponentIdentifier -> TypeSource(file, SourceType.PROJECT_DEPENDENCY, cid.projectPath)
        else -> TypeSource(file, SourceType.UNKNOWN, cid.displayName)
    }

fun File.source() = TypeSource(this, SourceType.UNKNOWN, name)

fun SourceDirectorySet.source() = TypeSource(classesDirectory.get().asFile, SourceType.SOURCE_SET, name)