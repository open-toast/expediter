package com.toasttab.expediter.gradle

import com.toasttab.expediter.types.ClassfileSource
import com.toasttab.expediter.types.ClassfileSourceType
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.file.SourceDirectorySet
import java.io.File

fun ResolvedArtifactResult.source() =
    when (val cid = id.componentIdentifier) {
        is ModuleComponentIdentifier -> ClassfileSource(file, ClassfileSourceType.EXTERNAL_DEPENDENCY, cid.displayName)
        is ProjectComponentIdentifier -> ClassfileSource(file, ClassfileSourceType.SUBPROJECT_DEPENDENCY, cid.projectPath)
        else -> ClassfileSource(file, ClassfileSourceType.UNKNOWN, cid.displayName)
    }

fun File.source() = ClassfileSource(this, ClassfileSourceType.UNKNOWN, name)

fun SourceDirectorySet.source() = ClassfileSource(classesDirectory.get().asFile, ClassfileSourceType.SOURCE_SET, name)
