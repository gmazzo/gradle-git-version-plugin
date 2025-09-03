package io.github.gmazzo.gitversion

import javax.inject.Inject
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.of

abstract class GitVersionExtensionReadonly @Inject constructor(
    private val providers: ProviderFactory,
) {

    abstract val tagPrefix: Provider<String>

    abstract val forceSnapshot: Provider<Boolean>

    abstract val initialVersion: Provider<String>

    abstract val version: Provider<String>

    abstract val versionModifier: Provider<String>

    abstract val forWholeBuild: Provider<Boolean>

    fun provider(producer: GitVersionProducer?): Provider<String> = providers.of(GitVersionValueSource::class) {
        parameters.tagPrefix.set(tagPrefix)
        parameters.initialVersion.set(initialVersion)
        parameters.forceSnapshot.set(forceSnapshot)
        parameters.versionModifier.set(versionModifier)
        parameters.versionProducer.set(producer)
    }

    final override fun toString() =
        version.get().toString()

}
