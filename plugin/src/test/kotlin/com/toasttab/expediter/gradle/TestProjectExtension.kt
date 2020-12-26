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

package com.toasttab.expediter.gradle

import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import java.io.Closeable
import java.io.File
import kotlin.io.path.createTempDirectory

private val NAMESPACE = ExtensionContext.Namespace.create("gradle-test-project")

class TestProject(
    val dir: File,
) : Closeable {
    override fun close() {
        dir.deleteRecursively()
    }
}

class TestProjectExtension : ParameterResolver, BeforeEachCallback {
    override fun beforeEach(context: ExtensionContext) {
        project(context)
    }

    private fun project(context: ExtensionContext) =
        context.getStore(NAMESPACE).getOrComputeIfAbsent("project") {
            val name = context.requiredTestMethod.name
            val tempProjectDir = createTempDirectory("junit-gradlekit").toFile()

            File("${System.getProperty("test-projects")}/$name").copyRecursively(target = tempProjectDir)

            TestProject(tempProjectDir)
        }

    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext) =
        parameterContext.parameter.type == TestProject::class.java

    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext) = project(extensionContext)
}
