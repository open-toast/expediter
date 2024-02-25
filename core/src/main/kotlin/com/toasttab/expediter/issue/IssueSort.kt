package com.toasttab.expediter.issue

object IssueSort {
    val DEFAULT: Comparator<Issue> = compareBy({
        it::class.java.name
    }, {
        it.target
    }, {
        it.caller
    })
}