/*
 * Copyright (c) 2023 Toast Inc.
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

package com.toasttab.expediter.scanner

import com.toasttab.model.types.ClassfileSource
import java.io.InputStream
import java.lang.Exception
import java.lang.RuntimeException
import java.util.jar.JarInputStream
import java.util.zip.ZipFile

class ClasspathScanner(
    private val elements: Iterable<ClassfileSource>
) {
    fun <T> scan(parse: (stream: InputStream, source: ClassfileSource) -> T): List<T> = elements.flatMap { types(it, parse) }

    private fun isClassFile(name: String) = name.endsWith(".class") &&
        !name.startsWith("META-INF/versions") && // mrjars not supported yet
        !name.endsWith("package-info.class") &&
        !name.endsWith("module-info.class")

    private fun <T> scanJarStream(stream: JarInputStream, source: ClassfileSource, parse: (stream: InputStream, source: ClassfileSource) -> T) = generateSequence { stream.nextJarEntry }
        .filter { isClassFile(it.name) }
        .map {
            try { parse(stream, source) } catch (e: Exception) {
                throw RuntimeException("could not parse ${it.name}", e)
            }
        }
        .toList()

    fun <T> scanJar(source: ClassfileSource, parse: (stream: InputStream, source: ClassfileSource) -> T): List<T> = source.file.inputStream().use {
        scanJarStream(JarInputStream(it), source, parse)
    }

    fun <T> scanAar(source: ClassfileSource, parse: (stream: InputStream, source: ClassfileSource) -> T): List<T> =
        ZipFile(source.file).use { aar ->
            when (val classesEntry = aar.getEntry("classes.jar")) {
                null -> emptyList()
                else -> {
                    JarInputStream(aar.getInputStream(classesEntry)).use {
                        scanJarStream(it, source, parse)
                    }
                }
            }
        }

    fun <T> scanClassDir(source: ClassfileSource, parse: (stream: InputStream, source: ClassfileSource) -> T): List<T> =
        source.file.walkTopDown().filter { isClassFile(it.name) }.map {
            it.inputStream().use { classStream ->
                parse(classStream, source)
            }
        }.toList()

    private fun <T> types(source: ClassfileSource, parse: (stream: InputStream, source: ClassfileSource) -> T) =
        when {
            source.file.isDirectory -> scanClassDir(source, parse)
            source.file.name.endsWith(".jar") -> scanJar(source, parse)
            source.file.name.endsWith(".aar") -> scanAar(source, parse)
            else -> emptyList()
        }
}
