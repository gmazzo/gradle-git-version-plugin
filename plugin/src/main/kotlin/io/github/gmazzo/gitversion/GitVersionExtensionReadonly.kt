package io.github.gmazzo.gitversion

import io.github.gmazzo.gitversion.GitVersionPlugin.Companion.EXTENSION_NAME
import org.gradle.api.provider.Property

/**
 * Allows accessing the root [GitVersionExtension] extension, when the plugin is loaded in different classloaders
 * (mostly when using [org.gradle.api.initialization.Settings.includeBuild]).
 */
internal class GitVersionExtensionReadonly(
    private val delegate: Any,
) : GitVersionExtensionInternal(owner = delegate.owner) {

    private fun failShouldConfigureInOwner(): Nothing =
        error("The `$EXTENSION_NAME` extension was created at $owner. Please configure it there")

    override val tagPrefix: Property<String>
        get() = failShouldConfigureInOwner()

    override val forceSnapshot: Property<Boolean>
        get() = failShouldConfigureInOwner()

    override val initialVersion: Property<String>
        get() = failShouldConfigureInOwner()

    override val versionProducer: Property<GitVersionProducer>
        get() = failShouldConfigureInOwner()

    override fun versionProducer(producer: GitVersionProducer) =
        failShouldConfigureInOwner()

    override val version: Property<String>
        get() = failShouldConfigureInOwner()

    override fun toString() =
        delegate.toString()

    private companion object {

        private val Any.owner
            get() = when (this) {
                is GitVersionExtensionInternal -> owner
                else -> runCatching { javaClass.getMethod("getOwner").invoke(this) as String }
                    .getOrElse { it.printStackTrace(); "unknown" }
            }

    }

}
