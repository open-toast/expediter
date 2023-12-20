package com.toasttab.expediter.gradle.service

import com.toasttab.expediter.provider.ClasspathApplicationTypesProvider
import com.toasttab.expediter.types.ApplicationTypeContainer
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import java.io.File
import java.util.concurrent.ConcurrentHashMap

abstract class ApplicationTypeCache : BuildService<BuildServiceParameters.None> {
    private val cache = ConcurrentHashMap<List<String>, ApplicationTypeContainer>()

    fun resolve(files: Iterable<File>) = cache.computeIfAbsent(files.map { it.path }.toList()) {
        ApplicationTypeContainer.create(ClasspathApplicationTypesProvider(files).types())
    }
}
