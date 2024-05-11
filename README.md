# Expediter

[![Github Actions](https://github.com/open-toast/expediter/actions/workflows/ci.yml/badge.svg)](https://github.com/open-toast/expediter/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.toasttab.expediter/core)](https://search.maven.org/artifact/com.toasttab.expediter/core)
[![Gradle Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/com/toasttab/expediter/core/maven-metadata.xml.svg?label=gradle-portal&color=yellowgreen)](https://plugins.gradle.org/plugin/com.toasttab.expediter)
[![codecov](https://codecov.io/gh/open-toast/expediter/graph/badge.svg?token=XYNHV3RVX0)](https://codecov.io/gh/open-toast/expediter)

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

This tool can detect certain binary incompatibilities at build time. Specifically, it will report

* Missing classes
* Duplicate classes
* Classes with missing superclasses and interfaces
* Classes extending final classes
* Missing methods and fields
* Static methods and fields accessed non-statically and vice-versa
* Inaccessible methods and fields

## Application vs platform classes

Conceptually, the inputs for running the tool are _application classes_ and _platform APIs_. The application
classes are the classes that need to be validated, and the platform APIs are the APIs provided by the runtime
environment of the application, e.g. the JVM or the Android SDK.

Typically, the application classes are the classes compiled directly by the project and the classes from the project's 
runtime dependencies that they reference.

The platform APIs can be specified by the JVM, a set of dependencies, or serialized type descriptors.

## Basic Gradle setup

The preferred mechanism for running Expediter is via the provided Gradle plugin.

```kotlin
plugins {
    id("com.toasttab.expediter") version <<version>>
}
```

## Application classes

By default, the application classes are selected from the main source set and the runtime dependencies of the project.
The classes compiled from the main source set and subproject dependencies are treated as _roots_. The roots and all
classes from the external runtime dependencies reachable from the roots then form the set of application classes.

The concept of roots makes it easier to filter out unused classes from third-party dependencies, which would otherwise
produce noise.

You can customize this behavior, e.g. change the Gradle configuration that describes the dependencies.

```kotlin
expediter {
    application {
        sourceSet("<< something other than main >>")
        configuration("<< something other than runtimeClasspath >>")
    }
}
```

For example, in an Android project, you will want to use a different configuration, such as `productionReleaseRuntime`.

You can also customize how the roots are chosen. This is the implicit default setup, where the roots are the classes
compiled from the current project and other subprojects of the same projects.

```kotlin
expediter {
    application {
        roots {
            project()
        }
    }
}
```

This is a different setup, where the roots are all classes compiled from the current project and all classes in its
runtime dependencies. Beware that with this setup, there will likely be a lot of noise from unused classes providig
optional functionality.

```kotlin
expediter {
    application {
        roots {
            all()
        }
    }
}
```

## Platform APIs

Platform APIs can be provided by the JVM or specified as a set of dependencies or published type descriptors in the
native [Expediter](proto/src/main/proto/toasttab/expediter/v1) or [Animal Sniffer](https://www.mojohaus.org/animal-sniffer/)
format.

### JVM platform APIs

To use platform APIs provided by a specific version of the JVM, use the `jvmVersion` property. You need to run the build
on a JVM version that is at least the specified `jvmVersion`.

```
expediter {
    platform {
        jvmVersion = 11
    }
}
```

### Serialized type descriptors

You can use published type descriptors instead of, or in addition to, the JVM to describe platform APIs. 

This is useful, for example, when checking compatibility with a specific Android SDK.

For Android compatibility, you may use a shorthand, which will set up the [Gummy Bears](https://github.com/open-toast/gummy-bears)-powered Android type descriptors.

```kotlin
expediter {
    platform {
        android {
            sdk = 21
        }
    }
}
```

Alternatively, configure the type descriptors explicitly.

```kotlin
expediter {
    platform {
        expediterConfiguration("_descriptors_")
    }
}

configurations.create("_descriptors_")

dependencies {
    add("_descriptors_", "com.toasttab.android:gummy-bears-api-21:0.8.0@expediter")
}
```

Expediter can also consume Animal Sniffer signatures

```kotlin
expediter {
    platform {
        animalSnifferConfiguration("_animal_sniffer_descriptors_")
    }
}

configurations.create("_animal_sniffer_descriptors_")

dependencies {
    add("_animal_sniffer_descriptors_", "com.toasttab.android:gummy-bears-api-21:0.8.0@signature")
}
```

### Dependencies as platform APIs

In addition to type descriptors and the JVM, plain dependencies can be used to describe platform APIs.

```kotlin
expediter {
    platform {
        configuration("runtimeClasspath")
    }
}
```

This can be used to emulate the default behavior of Animal Sniffer, where only the project classes are validated
against platform APIs, but dependencies are not.

For example, to validate that a library's source code is compatible with a specific version of Android without
validating the library's dependencies, use the setup below.

```kotlin
expediter {
    application {
        sourceSet("main")
    }
    platform {
        androidSdk = 21
        configuration("runtimeClasspath")
    }
}
```

### Fallback platform APIs

If no platform APIs are explicitly specified, the Gradle plugin will fall back to using the platform classloader
of the JVM running the build. This setup is rarely useful and will emit a warning.

## Run the task

Now you can run

```shell
./gradlew expedite
```

which will generate a report of binary incompatibilities under `build/expediter.json`.

## Ignores

Typically, there is a lot of noise in the report from unused parts of libraries and missing optional dependencies.

You can filter out issues using the `Ignore` API or by checking in a file with known issues.

```kotlin
expediter {
    ignore {
        targetStartsWith("some/unused/Type")
        file = "knownIssues.json"
    }
}
```

## Failing the build

Once you tune the ignores, you can make the plugin fail the build if any issues are still found.

```kotlin
expediter {
    failOnIssues = true
}
```

## Executing multiple checks

Expediter can execute checks for multiple combinations of application and platform types within the same 
project.

For example, to check application classes against Java 11 and Android SDK 21.

```kotlin
expediter {
    check("jvm") {
        platform {
            jvmVersion = 11
        }
    }
    
    check("android") {
        platform {
            android {
                sdk = 21
            }
        }
    }
}
```

The output of the `jvm` check will be written into `build/expediter-jvm.json`, and the output of the
`android` check will be written into `build/expediter-android.json`.

## CLI

Expediter ships a simple CLI. 

```
java -jar cli-0.0.21-all.jar \
  --project-classes=classes/ \
  --libraries=lib/lib1.jar \
  --libraries=lib/lib2.jar \
  --jvm-platform=17 \
  --output=expediter.json
```
