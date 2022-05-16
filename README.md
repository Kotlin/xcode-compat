[![Obsolete project](https://jb.gg/badges/obsolete.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)

# Kotlin Xcode compatibility Gradle plugin

The plugin is used by [AppCode](https://jetbrains.com/appcode) to set up Kotlin/Native project along with Xcode

# Sources

A multi-build sample with:

 1. [plugin](./plugin) a Gradle plugin implemented in Kotlin and taking advantage of the `kotlin-dsl` plugin,
 2. [consumer](./consumer) a build that uses the Gradle plugin above.

Run with:

    ./gradlew consumer

This will build and publish the Gradle `plugin` locally ; and then run the task contributed by this plugin in the `consumer` build. 

# License

Apache 2.0. See LICENSE file in the repostiory for details

