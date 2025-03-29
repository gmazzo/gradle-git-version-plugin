package io.github.gmazzo.gitversion

import org.gradle.api.Named
import org.gradle.api.provider.Property

abstract class GitVersionExtension() : Named {

    abstract val tagPrefix: Property<String>

    abstract val forceSnapshot : Property<Boolean>

    abstract val initialVersion: Property<String>

    abstract val versionProducer: Property<Class<out GitVersionValueSource>>

    abstract val version: Property<String>

    override fun toString() = version.get().toString()

}
