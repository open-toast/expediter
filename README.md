# Expediter

[![Github Actions](https://github.com/open-toast/expediter/actions/workflows/ci.yml/badge.svg)](https://github.com/open-toast/expediter/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.toasttab.expediter/core)](https://search.maven.org/artifact/com.toasttab.expediter/core)
[![Gradle Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/com/toasttab/expediter/plugin/maven-metadata.xml.svg?label=gradle-portal&color=yellowgreen)](https://plugins.gradle.org/plugin/com.toasttab.expediter)

Checks for binary incompatibilities between your application classes, application dependencies, and platform APIs at build time.

## Before you use it

This project is under active development. All API contracts are subject to change.

## Background

A typical Java-based application, server-side or Android, is composed of application code and a bunch of interdependent
libraries. One day, you bump the version of library libX in your application, which transitively bumps the version 
of library libY. This new version of libY changes the signature of a method which another library, libZ, happens 
to be using. In the ideal world, this would never happen, because you are enforcing version ranges, and everyone
is using semver correctly. But in the real world, you have a runtime error on your hands and you won't catch it until 
you exercise a specific code path in production or - hopefully - tests.

This tool can detect _some_ binary incompatibilities at build time. Specifically, it can report

* Missing classes
* Duplicate classes
* Classes with missing superclasses and interfaces
* Missing methods and fields
* Static methods and fields accessed non-statically and vice-versa
* Inaccessible methods and fields (partially)

## Application vs platform classes

Conceptually, the inputs for running the tool are _application classes_ and _platform APIs_. The application
classes are the classes that need to be validated, and the platform APIs are the APIs provided by the runtime
environment of the application, e.g. the JVM or the Android SDK.

Typically, the application classes are the classes compiled directly by the project and its runtime dependencies,
and the platform APIs are the classes provided by the JVM's platform classloader. However, platform APIs can
also be specified as a set of AnimalSniffer signatures.

## Setup

The most convenient way to run this tool is via the provided Gradle plugin.

```kotlin
plugins {
    id("com.toasttab.expediter") version <<version>>
}
```

### Application classes

By default, the application classes are the classes from the main source set and the runtime dependencies of the project.

You can customize this behavior, e.g. change the Gradle configuration that describes the dependencies.

```kotlin
expediter {
    application {
        configuration("<< something other than runtimeClasspath >>")
    }
}
```

For example, in an Android project, you may want to use the `productionReleaseRuntime` configuration.

### JVM platform APIs

By default, the platform APIs are defined by the JVM running the build. You can configure a specific version of the
JVM instead. Then only the APIs provided by that specific version of the JVM will be included. 

```
expediter {
    platform {
        jvmVersion = 11
    }
}
```

Setting an explicit `jvmVersion` is recommended unless checking Android compatibility or using custom signatures, see below.

### Animal Sniffer and Android support

You can use AnimalSniffer signatures instead of, or in addition to, the current JVM to describe platform APIs. This is useful, for example, when checking compatibility with a specific Android SDK.

For Android compatibility, you may use a shorthand, which will set up the [Gummy Bears](https://github.com/open-toast/gummy-bears)-powered Android signatures.

```kotlin
expediter {
    platform {
        androidSdk = 21
    }
}
```

Alternatively, configure the AnimalSniffer signatures explicitly.

```kotlin
expediter {
    platform {
        animalSnifferConfiguration("animalSniffer")
    }
}

configurations.create("animalSniffer")

dependencies {
    add("animalSniffer", "com.toasttab.android:gummy-bears-api-21:0.5.1@signature")
}
```

### Run the task

Now you can run

```shell
./gradlew expedite
```

which will generate a report of binary incompatibilities under `build/expediter.json`.

### Ignores

Typically, there is a lot of noise in the report from unused parts of libraries and missing optional dependencies.

You can filter out issues using the Ignore DSL or by checking in a file with known issues.

```kotlin
expediter {
    ignore = Ignore.Type.StartsWith("some/unused/Type")
    ignoreFile = "knownIssues.json"
}
```

### Failing the build

Once you tune the ignores, you can make the plugin fail the build if any issues are still found.

```kotlin
expediter {
    failOnIssues = true
}
```
