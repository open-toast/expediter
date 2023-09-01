package com.toasttab.expediter.types

import com.toasttab.expediter.TypeParsers
import java.nio.file.Paths
import java.util.jar.JarFile
import kotlin.io.path.pathString

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
class JvmTypeProvider private constructor(
    private val ctSym: JarFile,
    private val paths: Map<String, String>
) : PlatformTypeProvider {
    override fun lookupPlatformType(name: String): TypeDescriptor? {
        return paths[name]?.let { path ->
            val entry = ctSym.getJarEntry(path)

            // files in the ct.sym archive are just stripped down class files,
            // so we can parse them directly
            ctSym.getInputStream(entry).use {
                TypeParsers.typeDescriptor(it)
            }
        }
    }

    companion object {
        /**
         * Creates a PlatformTypeProvider roughly equivalent to `javac --release jvmTarget`.
         * If the target matches the version of the current JVM, use the platform classloader.
         * Else, load the types from `$JAVA_HOME/lib/ct.sym`, like `javac --release` does.
         */
        fun forTarget(jvmTarget: Int): PlatformTypeProvider {
            val ctSym = findCtSymFile()
            // files are prefixed with the version, 9=9, 10=A, 11=B, etc.
            val jvmVersion = jvmTarget.digitToChar(36)

            // having e.g. B/system-modules indicates that we're running on Java 11
            return if (ctSym.getJarEntry("$jvmVersion/system-modules") != null) {
                PlatformClassloaderTypeProvider
            } else {
                JvmTypeProvider(ctSym, nameToPath(ctSym, jvmVersion))
            }
        }
        private fun findCtSymFile() = JarFile(Paths.get(System.getProperty("java.home"), "lib", "ct.sym").pathString)

        /**
         * build a map from jvm type name to the location of the signature file in the ct.sym archive
         */
        private fun nameToPath(ctSym: JarFile, jvmVersion: Char): Map<String, String> {
            val paths = mutableMapOf<String, String>()

            for (entry in ctSym.entries()) {
                // the name of each entry is `$versions/$module/$name.sig`
                // where `versions` is the string with all supported versions, e.g. 89A for java 8-10,
                // `module` is the name of the java module, e.g. java.base, and is optional,
                // and `name` is the regular, `/`-separated JVM name
                if (entry.name.endsWith(".sig")) {
                    var separatorIdx = entry.name.indexOf('/')

                    val encodedVersion = entry.name.substring(0, separatorIdx)

                    if (encodedVersion.contains(jvmVersion)) {
                        val nextSeparatorIdx = entry.name.indexOf('/', startIndex = separatorIdx + 1)

                        if (nextSeparatorIdx >= 0 && entry.name.substring(separatorIdx + 1, nextSeparatorIdx)
                                .contains('.')
                        ) {
                            separatorIdx = nextSeparatorIdx
                        }

                        paths[entry.name.substring(separatorIdx + 1, entry.name.length - 4)] = entry.name
                    }
                }
            }

            return paths
        }
    }
}
