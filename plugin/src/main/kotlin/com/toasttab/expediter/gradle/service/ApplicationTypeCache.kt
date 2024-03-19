package com.toasttab.expediter.gradle.service

import com.toasttab.expediter.provider.ClasspathApplicationTypesProvider
import com.toasttab.expediter.types.ApplicationTypeContainer
import com.toasttab.model.types.ClassfileSource
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import java.util.concurrent.ConcurrentHashMap

private typealias CacheKey = List<String>
private fun Iterable<ClassfileSource>.key() = map { it.file.path }.toList()

abstract class ApplicationTypeCache : BuildService<BuildServiceParameters.None> {
    private val cache = ConcurrentHashMap<CacheKey, ApplicationTypeContainer>()

    fun resolve(files: Iterable<ClassfileSource>) = cache.computeIfAbsent(files.key()) {
        ApplicationTypeContainer.create(ClasspathApplicationTypesProvider(files).types())
    }
}
