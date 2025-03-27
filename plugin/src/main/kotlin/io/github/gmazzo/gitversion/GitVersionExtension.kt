package io.github.gmazzo.gitversion

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property

abstract class GitVersionExtension() {

    abstract val gitRootDirectory: DirectoryProperty

    abstract val tagPrefix: Property<String>

    abstract val version: Property<String>

    override fun toString() = version.get().toString()

}
