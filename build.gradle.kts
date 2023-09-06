plugins {
    `nexus-staging-conventions`
    `jacoco-report-aggregation`
}

repositories {
    mavenCentral()
}

group = "com.toasttab.expediter"

dependencies {
    jacocoAggregation(projects.core)
    jacocoAggregation(projects.tests)
    jacocoAggregation(projects.plugin)
}

reporting {
    reports {
        val testCodeCoverageReport by creating(JacocoCoverageReport::class) {
            testType.set(TestSuiteType.UNIT_TEST)
        }
    }
}