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

import com.toasttab.expediter.gradle.config.ExpediterExtension
import com.toasttab.expediter.gradle.service.ApplicationTypeCache
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.kotlin.dsl.withType

class ExpediterPlugin : Plugin<Project> {
    private fun Project.sourceSet(sourceSet: String) = extensions.getByType<SourceSetContainer>().getByName(sourceSet)

    override fun apply(project: Project) {
        val cache = project.gradle.sharedServices.registerIfAbsent("expediterTypeCache", ApplicationTypeCache::class.java) { }
        project.extensions.create<ExpediterExtension>("expediter", cache)

        project.tasks.register("expedite") {
            dependsOn(project.tasks.withType<ExpediterTask>())
        }

        project.tasks.named("check") {
            dependsOn("expedite")
        }
    }
}
