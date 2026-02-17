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

package com.toasttab.expediter.provider

import protokt.v1.toasttab.expediter.v1.TypeDescriptor

class InMemoryPlatformTypeProvider private constructor(
    private val types: Map<String, TypeDescriptor>
) : PlatformTypeProvider {
    constructor(types: Collection<TypeDescriptor>) : this(types.associateBy { it.name })

    override fun lookupPlatformType(name: String) = types[name]
}
