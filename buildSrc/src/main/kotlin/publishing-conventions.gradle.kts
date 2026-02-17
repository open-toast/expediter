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

import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType

plugins {
    id("maven-publish")
    id("signing")
}

group = rootProject.group
version = rootProject.version

configure<JavaPluginExtension> {
    withSourcesJar()
    withJavadocJar()
}

tasks.named<Jar>("javadocJar") {
    from("$rootDir/README.md")
    archiveClassifier.set("javadoc")
}

publishing {
    publications {
        withType<MavenPublication> {
            artifactId = project.name
            version = project.version.toString()
            groupId = project.group.toString()

            pom {
                name.set(ProjectInfo.name)
                description.set(ProjectInfo.description)
                url.set(ProjectInfo.url)
                scm {
                    url.set(ProjectInfo.url)
                }
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("Toast")
                        name.set("Toast Open Source")
                        email.set("opensource@toasttab.com")
                    }
                }
            }
        }
    }
}

signing {
    if (isRelease() && Pgp.key != null) {
        useInMemoryPgpKeys(Pgp.key, Pgp.password)

        project.publishing.publications.withType<MavenPublication> {
            sign(this)
        }
    } else {
        isRequired = false
    }
}
