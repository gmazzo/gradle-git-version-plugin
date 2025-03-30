package io.github.gmazzo.gitversion

import org.gradle.api.provider.Property

abstract class GitVersionExtension : GitVersionExtensionReadonly() {

    abstract override val tagPrefix: Property<String>

    abstract override val forceSnapshot : Property<Boolean>

    abstract override val initialVersion: Property<String>

    abstract val versionProducer: Property<GitVersionProducer>

    fun versionProducer(producer: GitVersionProducer) {
        versionProducer.set(producer)
    }

    abstract override val version: Property<String>

    abstract override val forWholeBuild: Property<Boolean>

}
