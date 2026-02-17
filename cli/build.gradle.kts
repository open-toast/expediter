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
    `library-publishing-conventions`
    alias(libs.plugins.shadow)
}

val ANDROID_LIB = "_android_lib_"
val ANDROID_DESCRIPTORS = "_android_descriptors_"

tasks {
    shadowJar {
        manifest {
            attributes["Main-Class"] = "com.toasttab.expediter.cli.ExpediterCliCommandKt"
        }
    }

    test {
        systemProperty("classes", sourceSets.main.get().kotlin.classesDirectory.get().asFile.path)
        systemProperty("libraries", configurations.getAt("testRuntimeClasspath").asPath)
        systemProperty("android-libraries", configurations.getAt(ANDROID_LIB).asPath)
        systemProperty("android-descriptors", configurations.getAt(ANDROID_DESCRIPTORS).asPath)
    }
}

configurations.create(ANDROID_LIB).isTransitive = false
configurations.create(ANDROID_DESCRIPTORS).isTransitive = false

dependencies {
    implementation(libs.clikt)
    implementation(libs.protobuf.java)
    implementation(projects.core)

    addProvider<MinimalExternalModuleDependency, ExternalModuleDependency>(ANDROID_DESCRIPTORS, libs.gummy.bears) {
        artifact {
            extension = "expediter"
        }
    }
    add(ANDROID_LIB, libs.timber)
}
