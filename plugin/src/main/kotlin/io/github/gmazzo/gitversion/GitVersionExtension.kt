package io.github.gmazzo.gitversion

import org.gradle.api.provider.Property

abstract class GitVersionExtension() {

    abstract val tagPrefix: Property<String>

    abstract val initialVersion: Property<String>

    abstract val version: Property<String>

    override fun toString() = version.get().toString()

}
