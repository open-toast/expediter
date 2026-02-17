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

package com.toasttab.expediter.issue

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.serializer
import java.io.InputStream
import java.io.OutputStream

@Serializable
class IssueReport(
    val name: String,
    val issues: List<Issue>
) {
    companion object {
        private val JSON = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            serializersModule = SerializersModule {
                polymorphic(Issue::class) {
                    defaultDeserializer {
                        Issue.UnknownIssue.serializer()
                    }
                }
            }
        }

        fun fromJson(string: String) = JSON.decodeFromString<IssueReport>(string)

        @OptIn(ExperimentalSerializationApi::class)
        fun fromJson(stream: InputStream) = JSON.decodeFromStream<IssueReport>(stream)
    }

    fun toJson() = JSON.encodeToString(this)

    @OptIn(ExperimentalSerializationApi::class)
    fun toJson(stream: OutputStream) = JSON.encodeToStream(this, stream)
}
