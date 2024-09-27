package com.toasttab.expediter.gradle

import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.ListProperty

interface TaskWithProjectOutputs {
    val projectOutputFiles: ListProperty<RegularFile>
    val projectOutputDirs: ListProperty<Directory>
}
