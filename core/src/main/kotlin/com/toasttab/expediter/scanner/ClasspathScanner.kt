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

import java.io.File
import java.io.InputStream
import java.lang.Exception
import java.lang.RuntimeException
import java.util.jar.JarInputStream
import java.util.zip.ZipFile

class ClasspathScanner(
    private val elements: Iterable<File>
) {
    fun <T> scan(parse: (stream: InputStream, source: String) -> T): List<T> = elements.flatMap { types(it, parse) }

    private fun isClassFile(name: String) = name.endsWith(".class") &&
        !name.startsWith("META-INF/versions") && // mrjars not supported yet
        !name.endsWith("package-info.class") &&
        !name.endsWith("module-info.class")

    private fun <T> scanJarStream(stream: JarInputStream, source: String, parse: (stream: InputStream, source: String) -> T) = generateSequence { stream.nextJarEntry }
        .filter { isClassFile(it.name) }
        .map {
            try { parse(stream, source) } catch (e: Exception) {
                throw RuntimeException("could not parse ${it.name}", e)
            }
        }
        .toList()

    fun <T> scanJar(path: File, parse: (stream: InputStream, source: String) -> T): List<T> = path.inputStream().use {
        scanJarStream(JarInputStream(it), path.name, parse)
    }

    fun <T> scanAar(path: File, parse: (stream: InputStream, source: String) -> T): List<T> =
        ZipFile(path).use { aar ->
            when (val classesEntry = aar.getEntry("classes.jar")) {
                null -> emptyList()
                else -> {
                    JarInputStream(aar.getInputStream(classesEntry)).use {
                        scanJarStream(it, path.name, parse)
                    }
                }
            }
        }

    fun <T> scanClassDir(path: File, parse: (stream: InputStream, source: String) -> T): List<T> =
        path.walkTopDown().filter { isClassFile(it.name) }.map {
            it.inputStream().use { classStream ->
                parse(classStream, path.name)
            }
        }.toList()

    private fun <T> types(path: File, parse: (stream: InputStream, source: String) -> T) =
        when {
            path.isDirectory -> scanClassDir(path, parse)
            path.name.endsWith(".jar") -> scanJar(path, parse)
            path.name.endsWith(".aar") -> scanAar(path, parse)
            else -> emptyList()
        }
}
