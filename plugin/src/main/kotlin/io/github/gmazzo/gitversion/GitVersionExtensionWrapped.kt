package io.github.gmazzo.gitversion

import io.github.gmazzo.gitversion.GitVersionPlugin.Companion.EXTENSION_NAME
import org.gradle.api.Named
import org.gradle.api.provider.Property

/**
 * Allows accessing the root [GitVersionExtension] extension, when the plugin is loaded in different classloaders
 * (mostly when using [org.gradle.api.initialization.Settings.includeBuild]).
 */
internal class GitVersionExtensionWrapped(
    private val delegate: Named,
) : GitVersionExtension(), Named by delegate {

    private fun failShouldConfigureInOwner(): Nothing =
        error("The `$EXTENSION_NAME` extension was created at '$name'. Please configure it there")

    override val tagPrefix: Property<String>
        get() = failShouldConfigureInOwner()

    override val forceSnapshot: Property<Boolean>
        get() = failShouldConfigureInOwner()

    override val initialVersion: Property<String>
        get() = failShouldConfigureInOwner()

    override val versionProducer: Property<Class<out GitVersionValueSource>>
        get() = failShouldConfigureInOwner()

    override val version: Property<String>
        get() = failShouldConfigureInOwner()

    override fun toString() =
        delegate.toString()

}
