package io.github.gmazzo.gitversion

import java.util.*
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

internal abstract class GitVersionBuildService :
    BuildService<BuildServiceParameters.None>,
    WeakHashMap<ExtensionAware, GitVersionExtensionReadonly>()
