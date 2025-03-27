package io.github.gmazzo.gitversion

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property

/**
 * Allows accessing the root [GitVersionExtension] extension, when the plugin is loaded in different classloaders
 * (mostly when using [org.gradle.api.initialization.Settings.includeBuild]).
 */
@Suppress("UNCHECKED_CAST")
internal class GitVersionExtensionReflected(
    private val delegate: Any
) : GitVersionExtension() {

    private val gitRootDirectoryGetter =
        delegate.javaClass.getMethod("getGitRootDirectory")

    private val tagPrefixGetter =
        delegate.javaClass.getMethod("getTagPrefix")

    private var versionProducerGetter =
        delegate.javaClass.getMethod("getVersionProducer")

    private val versionGetter =
        delegate.javaClass.getMethod("getVersion")

    override val gitRootDirectory: DirectoryProperty
        get() = gitRootDirectoryGetter.invoke(delegate) as DirectoryProperty

    override val tagPrefix: Property<String>
        get() = tagPrefixGetter.invoke(delegate) as Property<String>

    override val versionProducer: Property<GitVersionProducer>
        get() = versionProducerGetter.invoke(delegate) as Property<GitVersionProducer>

    override val version: Property<String>
        get() = versionGetter.invoke(delegate) as Property<String>

}
