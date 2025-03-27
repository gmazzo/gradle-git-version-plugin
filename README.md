![GitHub](https://img.shields.io/github/license/gmazzo/gradle-git-version-plugin)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.gmazzo.gitversion/io.github.gmazzo.gitversion.gradle.plugin)](https://central.sonatype.com/artifact/io.github.gmazzo.gitversion/io.github.gmazzo.gitversion.gradle.plugin)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/io.github.gmazzo.gitversion)](https://plugins.gradle.org/plugin/io.github.gmazzo.gitversion)
[![Build Status](https://github.com/gmazzo/gradle-git-version-plugin/actions/workflows/ci-cd.yaml/badge.svg)](https://github.com/gmazzo/gradle-git-version-plugin/actions/workflows/ci-cd.yaml)
[![Coverage](https://codecov.io/gh/gmazzo/gradle-git-version-plugin/branch/main/graph/badge.svg?token=D5cDiPWvcS)](https://codecov.io/gh/gmazzo/gradle-git-version-plugin)
[![Users](https://img.shields.io/badge/users_by-Sourcegraph-purple)](https://sourcegraph.com/search?q=content:io.github.gmazzo.gitversion+-repo:github.com/gmazzo/gradle-git-version-plugin)

# gradle-git-version-plugin

An opinionated Gradle plugin to provide a project version using Git tags.

# Usage

Apply the plugin:

```kotlin
plugins {
    id("io.github.gmazzo.gitversion") version "<latest>"
}
```

Then `project.version` on all projects will be set to the latest tag reachable from the current commit.

## Version computing
The plugin will rely on the command `git describe --tags --always --match <tagPrefix>*` to compute the version. Than means:
1) Will take the closest tag reachable from the current commit, matching the given prefix.
2) If the current commit is tagged, the version will be the tag itself.
3) If the current commit is not tagged or the working tree is dirty, the version will be the tag name followed by `-SNAPSHOT`.
