![GitHub](https://img.shields.io/github/license/gmazzo/gradle-git-version-plugin)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.gmazzo.gitversion/io.github.gmazzo.gitversion.gradle.plugin)](https://central.sonatype.com/artifact/io.github.gmazzo.gitversion/io.github.gmazzo.gitversion.gradle.plugin)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/io.github.gmazzo.gitversion)](https://plugins.gradle.org/plugin/io.github.gmazzo.gitversion)
[![Build Status](https://github.com/gmazzo/gradle-git-version-plugin/actions/workflows/ci-cd.yaml/badge.svg)](https://github.com/gmazzo/gradle-git-version-plugin/actions/workflows/ci-cd.yaml)
[![Coverage](https://codecov.io/gh/gmazzo/gradle-git-version-plugin/branch/main/graph/badge.svg?token=D5cDiPWvcS)](https://codecov.io/gh/gmazzo/gradle-git-version-plugin)
[![Users](https://img.shields.io/badge/users_by-Sourcegraph-purple)](https://sourcegraph.com/search?q=content:io.github.gmazzo.gitversion+-repo:github.com/gmazzo/gradle-git-version-plugin)

[![Contributors](https://contrib.rocks/image?repo=gmazzo/gradle-git-version-plugin)](https://github.com/gmazzo/gradle-git-version-plugin/graphs/contributors)

# gradle-git-version-plugin

An opinionated Gradle plugin to provide a project version using Git tags.

# Usage

Apply the plugin:

```kotlin
plugins {
  id("io.github.gmazzo.gitversion") version "<latest>"

  gitVersion {
    tagPrefix = "v" // Optional, default is "v"
    initialVersion = "0.1.0-SNAPSHOT" // Optional, default is "0.1.0-SNAPSHOT"
  }
}
```

Then `project.version` on all projects will be set to the latest tag reachable from the current commit.

## Version computing
The version is computed using `git describe` commands as follows:
1) If the current commit is tagged, the version is the tag name.
2) If the current commit is not tagged, the version is the tag name of the latest tag reachable from the current commit suffixed with `-SNAPSHOT`.
3) If there are no tags, the version is `0.1.0-SNAPSHOT`.

## Included builds support
This plugin can be applied at `build.gradle` or `settings.gradle` script.
If you apply it as well in inside an `includedBuild`'s build script, the same version instance will be configured to it.

## Multi-version support
This plugin can also be used to provide independent versioning on modules hierarchies.
To archive this, you should **avoid** applying it at the root `build.gradle` (or at `settings.gradle`), and apply it only on the submodules that you want to version instead:

For insance, given the following project structure:
```
build.gradle
settings.gradle
foo/build.gradle
bar/build.gradle
bar/api/build.gradle
bar/impl/build.gradle
```

You can version `foo` and `bar` independently by applying the plugin only in their respective `build.gradle` files:
```kotlin
// at foo/build.gradle
plugins {
  id("io.github.gmazzo.gitversion") version "<latest>"

  gitVersion {
    tagPrefix = "foo-v"
  }
}
```
and
```kotlin
// at bar/build.gradle
plugins {
  id("io.github.gmazzo.gitversion") version "<latest>"

  gitVersion {
    tagPrefix = "bar-v"
  }
}
```
> [!NOTE]
> Note that `bar`, `bar:api` and `bar:impl` will all share the same version

## Customizing the version
You can customize the version by setting the `tagPrefix` and `initialVersion` properties in the `gitVersion` block.

Also a custom `versionProducer` can be set by extending the `GitVersionValueSource` class.

For instance, the following code will decorate the version with the current branch name:
```kotlin
gitVersion.versionProducer {
  val branch = command("git", "rev-parse", "--abbrev-ref", "HEAD")

  "$candidate+$branch"
}
```

## Versining on Android
An opinionated approach: `versionName` will be the computed git version, and `versionCode` the number of tags/versions in the branch history

```kotlin
android {
    defaultConfig {
        versionCode = gitVersion.provider { tagsCount().toString() }.get().toInt()
        versionName = gitVersion.toString()
```
