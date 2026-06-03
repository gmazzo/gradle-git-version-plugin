package io.github.gmazzo.gitversion

import javax.inject.Inject
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory

internal abstract class GitVersionExtensionImpl @Inject internal constructor(
    providers: ProviderFactory,
) : GitVersionExtension, GitVersionExtensionReadonlyImpl(providers) {

    abstract override val tagPrefix: Property<String>

    abstract override val forceSnapshot: Property<Boolean>

    abstract override val initialVersion: Property<String>

    abstract override val versionModifier: Property<String>

    abstract override val versionProducer: Property<GitVersionProducer>

    override fun versionProducer(producer: GitVersionProducer) {
        versionProducer.set(producer)
    }

    abstract override val version: Property<String>

    abstract override val forWholeBuild: Property<Boolean>

}
