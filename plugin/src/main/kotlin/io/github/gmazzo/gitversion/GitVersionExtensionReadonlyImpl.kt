package io.github.gmazzo.gitversion

import javax.inject.Inject
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.provider.ValueSourceSpec
import org.gradle.kotlin.dsl.of

internal abstract class GitVersionExtensionReadonlyImpl @Inject internal constructor(
    protected val providers: ProviderFactory,
) : GitVersionExtensionReadonly {

    abstract override val tagPrefix: Provider<String>

    abstract override val forceSnapshot: Provider<Boolean>

    abstract override val initialVersion: Provider<String>

    abstract override val version: Provider<String>

    abstract override val versionModifier: Provider<String>

    abstract override val forWholeBuild: Provider<Boolean>

    override fun provider(producer: GitVersionProducer?): Provider<String> = provider {
        parameters.versionProducer.set(producer)
    }

    override fun provider(producer: Provider<GitVersionProducer>): Provider<String> = provider {
        parameters.versionProducer.set(producer)
    }

    private fun provider(configure: ValueSourceSpec<GitVersionValueSource.Params>.() -> Unit): Provider<String> =
        providers.of(GitVersionValueSource::class) {
            parameters.tagPrefix.set(tagPrefix)
            parameters.initialVersion.set(initialVersion)
            parameters.forceSnapshot.set(forceSnapshot)
            parameters.versionModifier.set(versionModifier)
            configure()
        }

    final override fun toString(): String =
        version.get()

}
