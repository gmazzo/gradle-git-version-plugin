package io.github.gmazzo.gitversion

import org.gradle.api.provider.Property

/**
 * Allows accessing the root [GitVersionExtension] extension, when the plugin is loaded in different classloaders
 * (mostly when using [org.gradle.api.initialization.Settings.includeBuild]).
 */
@Suppress("UNCHECKED_CAST")
internal class GitVersionExtensionReflected(
    private val delegate: Any
) : GitVersionExtension() {

    private val tagPrefixGetter =
        delegate.javaClass.getMethod("getTagPrefix")

    private val initialVersionGetter =
        delegate.javaClass.getMethod("getInitialVersion")

    private val versionGetter =
        delegate.javaClass.getMethod("getVersion")

    override val tagPrefix: Property<String>
        get() = tagPrefixGetter.invoke(delegate) as Property<String>

    override val initialVersion: Property<String>
        get() = initialVersionGetter.invoke(delegate) as Property<String>

    override val version: Property<String>
        get() = versionGetter.invoke(delegate) as Property<String>

}
