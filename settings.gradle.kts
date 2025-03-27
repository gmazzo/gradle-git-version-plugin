pluginManagement {
    includeBuild("plugin")
}

plugins {
    id("io.github.gmazzo.gitversion")
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        mavenCentral()
    }
}

rootProject.name = "gradle-git-version-plugin"

include("demo")
