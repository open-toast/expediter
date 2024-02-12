package com.toasttab.expediter.types

import java.io.File

data class TypeSource (
    val file: File,
    val type: SourceType,
    val name: String
)

enum class SourceType {
    UNKNOWN,
    SOURCE_SET,
    PROJECT_DEPENDENCY,
    DEPENDENCY
}