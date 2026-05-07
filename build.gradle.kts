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

import org.gradle.api.publish.maven.tasks.PublishToMavenRepository

plugins {
    `nexus-staging-conventions`
    `jacoco-report-aggregation`
}

repositories {
    mavenCentral()
}

// Work around a config-cache bug in testkit-plugin 0.0.18: its publishOnlyIf captures
// the PublishToMavenRepository task and reads task.publication at execution time,
// which Gradle discards after storing the configuration cache. Replace the onlyIf
// predicate with one derived from the task name (which encodes publication and
// repository names). testkit registers its buggy onlyIf inside the `:plugin` project's
// afterEvaluate; projectsEvaluated fires after all afterEvaluate callbacks, so our
// setOnlyIf runs last and wins.
gradle.projectsEvaluated {
    allprojects {
        tasks.withType<PublishToMavenRepository>().configureEach {
            val match = Regex("^publish(.+)PublicationTo(.+)Repository$").matchEntire(name) ?: return@configureEach
            val publicationName = match.groupValues[1].replaceFirstChar(Char::lowercaseChar)
            val repositoryName = match.groupValues[2].replaceFirstChar(Char::lowercaseChar)
            val isIntegrationRepo = repositoryName.startsWith("testkitIntegrationFor")
            val isIntegrationPublication = publicationName == "testkitIntegration"
            val isPluginMarker = publicationName.endsWith("PluginMarkerMaven")
            setOnlyIf("testkit integration publication routing") {
                when {
                    isIntegrationPublication -> isIntegrationRepo
                    isIntegrationRepo -> isPluginMarker
                    else -> true
                }
            }
        }
    }
}

group = "com.toasttab.expediter"

dependencies {
    jacocoAggregation(projects.cli)
    jacocoAggregation(projects.core)
    jacocoAggregation(projects.tests)
    jacocoAggregation(projects.plugin)
}

reporting {
    reports {
        val testCodeCoverageReport by creating(JacocoCoverageReport::class) {
            testSuiteName = "test"
        }
    }
}