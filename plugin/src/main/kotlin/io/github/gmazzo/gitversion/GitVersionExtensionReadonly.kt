package io.github.gmazzo.gitversion

import org.gradle.api.provider.Provider

abstract class GitVersionExtensionReadonly {

    abstract val tagPrefix: Provider<String>

    abstract val forceSnapshot : Provider<Boolean>

    abstract val initialVersion: Provider<String>

    abstract val version: Provider<String>

    abstract val forWholeBuild: Provider<Boolean>

    final override fun toString() =
        version.get().toString()

}
