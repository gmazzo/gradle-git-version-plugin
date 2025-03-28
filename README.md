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
The version is computed using `git describe` commands as follows:
1) If the current commit is tagged, the version is the tag name.
2) If the current commit is not tagged, the version is the tag name of the latest tag reachable from the current commit suffixed with `-SNAPSHOT`.
3) If there are no tags, the version is `0.1.0-SNAPSHOT`.
