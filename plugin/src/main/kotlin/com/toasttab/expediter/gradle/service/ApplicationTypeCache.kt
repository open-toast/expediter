/*
 * Copyright (c) 2026 Toast Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.toasttab.expediter.gradle.service

import com.toasttab.expediter.provider.ClasspathApplicationTypesProvider
import com.toasttab.expediter.types.ApplicationTypeContainer
import com.toasttab.expediter.types.ClassfileSource
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
