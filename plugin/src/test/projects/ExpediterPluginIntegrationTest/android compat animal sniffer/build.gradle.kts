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
    java
    id("com.toasttab.expediter")
    id("com.toasttab.testkit.coverage")
}

repositories {
    mavenCentral()
}

expediter {
    failOnIssues = true

    platform {
        animalSnifferConfiguration("_animal_sniffer_descriptors_")
    }
}

configurations.create("_animal_sniffer_descriptors_")

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    add("_animal_sniffer_descriptors_", "com.toasttab.android:gummy-bears-api-21:0.8.0@signature")
}
