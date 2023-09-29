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

package com.toasttab.expediter

import com.toasttab.expediter.types.ApplicationType
import java.io.File
import java.lang.Exception
import java.lang.RuntimeException
import java.util.jar.JarInputStream
import java.util.zip.ZipFile

class ClasspathScanner(
    private val elements: Iterable<File>
) : ApplicationTypesProvider {
    override fun types(): List<ApplicationType> = elements.flatMap { types(it) }

    private fun isClassFile(name: String) = name.endsWith(".class") &&
        !name.startsWith("META-INF/versions") && // mrjars not supported yet
        !name.endsWith("package-info.class") &&
        !name.endsWith("module-info.class")

    private fun scanJarStream(stream: JarInputStream, source: String) = generateSequence { stream.nextJarEntry }
        .filter { isClassFile(it.name) }
        .map {
            try { TypeParsers.applicationType(stream, source) } catch (e: Exception) {
                throw RuntimeException("could not parse ${it.name}", e)
            }
        }
        .toList()

    private fun scanJar(path: File): List<ApplicationType> = path.inputStream().use {
        scanJarStream(JarInputStream(it), path.name)
    }

    private fun scanAar(path: File): List<ApplicationType> =
        ZipFile(path).use { aar ->
            when (val classesEntry = aar.getEntry("classes.jar")) {
                null -> emptyList()
                else -> {
                    JarInputStream(aar.getInputStream(classesEntry)).use {
                        scanJarStream(it, path.name)
                    }
                }
            }
        }

    private fun scanClassDir(path: File): List<ApplicationType> =
        path.walkTopDown().filter { isClassFile(it.name) }.map {
            it.inputStream().use { classStream ->
                TypeParsers.applicationType(classStream, path.name)
            }
        }.toList()

    private fun types(path: File) =
        when {
            path.isDirectory -> scanClassDir(path)
            path.name.endsWith(".jar") -> scanJar(path)
            path.name.endsWith(".aar") -> scanAar(path)
            else -> emptyList()
        }
}
