/*
 * Copyright (c) 2024 Toast Inc.
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

import com.toasttab.model.issue.Issue

object IssueOrder {
    val CALLER: Comparator<Issue> = compareBy({
        it.caller
    }, {
        it.target
    })

    val TYPE: Comparator<Issue> = compareBy({
        it::class.java.name
    }, {
        it.target
    }, {
        it.caller
    })
}
