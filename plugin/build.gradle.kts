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

plugins {
    `kotlin-conventions`
    `kotlin-dsl`
    `plugin-publishing-conventions`
    alias(libs.plugins.testkit.plugin)
    alias(libs.plugins.build.config)
}

dependencies {
    implementation(projects.core)
    implementation(projects.animalSnifferFormat)
    implementation(libs.protobuf.java)

    compileOnly(libs.agp)

    testImplementation(libs.testkit.junit5)
}

testkitTests {
    replaceToken("KOTLIN_VERSION", libs.versions.kotlin.get())
    replaceToken("PROTOKT_VERSION", libs.versions.protokt.get())
    replaceToken("PROTOBUF_VERSION", libs.versions.protobuf.get())
}

buildConfig {
    packageName.set("com.toasttab.expediter.gradle")
    buildConfigField("String", "GUMMY_BEARS_VERSION", "\"${libs.versions.gummy.bears.get()}\"")
}

gradlePlugin {
    plugins {
        create("expediter") {
            id = "com.toasttab.expediter"
            implementationClass = "com.toasttab.expediter.gradle.ExpediterPlugin"
            description = ProjectInfo.description
            displayName = ProjectInfo.name
            tags = listOf("abi", "compatibility")
        }
    }
}
