package io.github.gmazzo.gitversion

import javax.inject.Inject
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory

public abstract class GitVersionExtension @Inject constructor(
    providers: ProviderFactory,
) : GitVersionExtensionReadonly(providers) {

    abstract override val tagPrefix: Property<String>

    abstract override val forceSnapshot: Property<Boolean>

    abstract override val initialVersion: Property<String>

    abstract override val versionModifier: Property<String>

    public abstract val versionProducer: Property<GitVersionProducer>

    public fun versionProducer(producer: GitVersionProducer) {
        versionProducer.set(producer)
    }

    abstract override val version: Property<String>

    abstract override val forWholeBuild: Property<Boolean>

}
