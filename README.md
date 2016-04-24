# Kawa Gradle Plugin

A Gradle plugin to build Kawa projects.  Integrates with the Android Plugin for
Gradle, making it easy to use Kawa in Android projects.

---

Applying the `com.medranocalvo.gradle.gradle-kawa-plugin` plugin does the following:

- Add the KawaCompile task.
- Add a top-level KawaOptions extension.

Applying the `com.medranocalvo.gradle.gradle-android-kawa-plugin` plugin does the following:

- Add a default Kawa sourceSet for each variant, and allow configuring it.
- Add a KawaCompile task for each variant, that takes care of compiling the scheme sources associated to the variant's sourceSet.
- Add a KawaOptions extensions to each buildType and productFlavor, allowing configuring the Kawa compile task for each of them.

## Installation

To install latest release, follow instructions at https://plugins.gradle.org/plugin/com.medranocalvo.gradle.gradle-kawa-plugin or https://plugins.gradle.org/plugin/com.medranocalvo.gradle.gradle-android-kawa-plugin.

To use the master version, clone the repository and run `./gradlew publishToMavenLocal`.  Then, in the Kawa project's build script enable the maven local repository `buildscript.repositories.mavenLocal()`.

## Configuration

### KawaCompile

The `KawaCompile` task takes care of compiling scheme source files.  It inherits from [AbstractCompile](https://docs.gradle.org/current/dsl/org.gradle.api.tasks.compile.AbstractCompile.html), (q.v.); adding the following properties:

- `language` :: `String`.  Kawa language to use. e.g. `scheme`, `r7rs`, etc.
- `inline` :: `Boolean`.  Whether to use the `--no-inline` flag.
- `warnings` :: `Set<String>`.  Warnings to enable, the suffixes of the `--warn-*` flags.

### KawaOptions

Used in the build script to configure Kawa.  It offers the following setters,
configuring the corresponding `KawaCompile` property:

- `language` :: `String`
- `inline` :: `Boolean`
- `warn` :: `String...`

### Conventions

The `com.medranocalvo.gradle.gradle-kawa-plugin` provides the `KawaCompile` task.  No conventions for regular Kawa projects are implemented yet.

### Android conventions

The `com.medranocalvo.gradle.gradle-android-kawa-plugin` uses the following conventions:

- Kawa compilation can be configured with the `build`, `android`, `buildType` and `productFlavor` levels, via the `KawaOptions` extension named `kawa`.  See the [example project](./examples/simple) for details.

- Kawa sources are looked for in `src/*/scm` where `*` is the variant name: _main_ , the _buildType_, the _productFlavor_, etc.  Source files must have a `.scm` extension.

### Examples

See the [example projects](./examples/) for details.

## Future work

- Test multiple versions of Kawa, Gradle and Android plugin.
- Add sample projects for testing.  We might use Gradle's TestKit.
- Support non-android projects.
- Publish to plugins.gradle.org/bintray.
- Retrieve Kawa from a maven repository.
- Allow passing extra flags to `kawa.repl`.
- Allow using a file extension different from `.scm`.
