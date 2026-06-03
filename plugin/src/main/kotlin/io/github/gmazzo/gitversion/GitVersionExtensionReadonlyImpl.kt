package io.github.gmazzo.gitversion

import javax.inject.Inject
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.of

internal abstract class GitVersionExtensionReadonlyImpl @Inject internal constructor(
    private val providers: ProviderFactory,
) : GitVersionExtensionReadonly {

    abstract override val tagPrefix: Provider<String>

    abstract override val forceSnapshot: Provider<Boolean>

    abstract override val initialVersion: Provider<String>

    abstract override val version: Provider<String>

    abstract override val versionModifier: Provider<String>

    abstract override val forWholeBuild: Provider<Boolean>

    override fun provider(producer: GitVersionProducer?): Provider<String> = providers.of(GitVersionValueSource::class) {
        parameters.tagPrefix.set(tagPrefix)
        parameters.initialVersion.set(initialVersion)
        parameters.forceSnapshot.set(forceSnapshot)
        parameters.versionModifier.set(versionModifier)
        parameters.versionProducer.set(producer)
    }

    final override fun toString(): String =
        version.get()

}
