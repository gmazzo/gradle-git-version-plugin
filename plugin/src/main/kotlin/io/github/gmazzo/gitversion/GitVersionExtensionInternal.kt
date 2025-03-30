package io.github.gmazzo.gitversion

import org.gradle.api.provider.Property

internal abstract class GitVersionExtensionInternal(val owner: String) : GitVersionExtension {

    abstract override val tagPrefix: Property<String>

    abstract override val forceSnapshot: Property<Boolean>

    abstract override val initialVersion: Property<String>

    abstract override val versionProducer: Property<GitVersionProducer>

    override fun versionProducer(producer: GitVersionProducer) {
        versionProducer.set(producer)
    }

    abstract override val version: Property<String>

    override fun toString() = version.get().toString()

}
