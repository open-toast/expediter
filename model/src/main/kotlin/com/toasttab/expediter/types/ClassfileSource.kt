package com.toasttab.expediter.types

import java.io.File

/**
 * Reference to a class file with additional metadata describing where the class file comes from.
 */
data class ClassfileSource (
    val file: File,
    val type: ClassfileSourceType,
    val name: String
)

enum class ClassfileSourceType {
    /** unmanaged by the build system, e.g. raw jar file */
    UNKNOWN,
    /** compiled from source in the current project/subproject */
    SOURCE_SET,
    /** different subproject of the same project */
    SUBPROJECT_DEPENDENCY,
    /** external dependency managed by the build system */
    EXTERNAL_DEPENDENCY
}