package com.toasttab.expediter.gradle.service

import com.toasttab.expediter.provider.ClasspathApplicationTypesProvider
import com.toasttab.expediter.types.ApplicationTypeContainer
import com.toasttab.expediter.types.TypeSource
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import java.io.File
import java.util.concurrent.ConcurrentHashMap

private typealias CacheKey = List<String>
private fun Iterable<TypeSource>.key() = map { it.file.path }.toList()

abstract class ApplicationTypeCache : BuildService<BuildServiceParameters.None> {
    private val cache = ConcurrentHashMap<CacheKey, ApplicationTypeContainer>()

    fun resolve(files: Iterable<TypeSource>) = cache.computeIfAbsent(files.key()) {
        ApplicationTypeContainer.create(ClasspathApplicationTypesProvider(files).types())
    }
}
