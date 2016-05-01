# Kawa Gradle Plugin

A Gradle plugin to build [Kawa][kawa] projects.  Integrates with the Android Plugin for Gradle, easing use of Kawa in Android projects.

Applying the `com.medranocalvo.gradle.gradle-kawa-plugin` plugin does the following:

- Add the `KawaCompile` task type.
- Add a `KawaOptions` extension to the project.

Applying the `com.medranocalvo.gradle.gradle-android-kawa-plugin` plugin does the following:

- Add a Kawa sourceSet for each variant.
- Add a task of type `KawaCompile` to each variant, taking care of compiling the scheme sources associated to the variant's sourceSets.
- Add `KawaOptions` extensions to each `buildType` and `productFlavor`, allowing configuring the Kawa compile task for each of them.

## Supported versions

Gradle versions `2.8`, `2.9`, `2.10`, `2.11`, `2.12` and `2.13`; and Android Plugin for Gradle versions `1.5.0`, `2.0.0` and `2.1.0` have been tested.  May your versions be missing, please [let us know](#improvements-and-issues).

## Installation

Place one of the following snippets after the `buildscript` block in the `build.gradle` file of your project:

~~~{.groovy}
/* File: build.gradle */
plugins {
    id "com.medranocalvo.gradle.gradle-kawa-plugin" version "0.9"
}
~~~

or

~~~{.groovy}
/* File: build.gradle */
plugins {
    id "com.medranocalvo.gradle.gradle-android-kawa-plugin" version "0.9"
}
~~~

For older Gradle versions, please follow instructions at either of https://plugins.gradle.org/plugin/com.medranocalvo.gradle.gradle-kawa-plugin https://plugins.gradle.org/plugin/com.medranocalvo.gradle.gradle-android-kawa-plugin.

### Development version

Unreleased versions can be used by cloning the repository and installing to the local Maven repository:

~~~{.bash}
$ git clone https://github.com/medranocalvo/gradle-kawa-plugin gradle-kawa-plugin
$ cd gradle-kawa-plugin
$ ./gradlew publishToMavenLocal
~~~

Make sure to enable the local Maven repository in the projects using the unreleased version:

~~~{.groovy}
/* File: build.gradle */
buildscript {
    repositories {
        mavenLocal()
    }
}
~~~

## Configuration

#### KawaCompile

The `KawaCompile` task takes care of compiling scheme source files.  It inherits from [AbstractCompile][AbstractCompile], (q.v.); adding the following properties:

- `language` :: `String`.  Kawa language to use. e.g. `scheme`, `r7rs`, etc.
- `inline` :: `Boolean`.  Whether to use the `--no-inline` flag.
- `warnings` :: `Set<String>`.  Warnings to enable, the suffixes of the `--warn-*` flags.

#### KawaOptions

Used in the build script to configure Kawa.  It offers the following setters,
configuring the corresponding `KawaCompile` property:

- `language` :: `String`
- `inline` :: `Boolean`
- `warn` :: `String...`

### Conventions

The `com.medranocalvo.gradle.gradle-kawa-plugin` provides the `KawaCompile` task.  No conventions for regular Kawa projects are implemented yet.

### Android conventions

The `com.medranocalvo.gradle.gradle-android-kawa-plugin` uses the following conventions:

- Kawa compilation can be configured with the `build`, `android`, `buildType`
  and `productFlavor` levels, via the `KawaOptions` extension named `kawa`.  See the [example projects][examples] for details.

- Kawa sources are looked for in `src/*/scm` where `*` is the variant name: _main_ , the _buildType_, the _productFlavor_, etc.  Source files must have a `.scm` extension.

### Examples

The [example projects][examples] showcase the features supported by this plugin, and are furthermore used for testing them.

## Improvements and issues

Please, let know of any potential improvements to the functionality or documentation, as well as any issues found via the [bugtracker][bugs].

### Wishlist

- Test multiple versions of Kawa.
- Support non-android projects.
- Retrieve Kawa from a maven repository.
- Allow passing extra flags to `kawa.repl`.
- Allow using a file extension different from `.scm`.

[kawa]: https://www.gnu.org/software/kawa
[bugs]: ../../issues
[examples]: ./examples/
[AbstractCompile]: https://docs.gradle.org/current/dsl/org.gradle.api.tasks.compile.AbstractCompile.html
