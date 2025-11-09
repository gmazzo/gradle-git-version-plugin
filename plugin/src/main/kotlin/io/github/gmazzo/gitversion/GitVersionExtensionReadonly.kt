package io.github.gmazzo.gitversion

import javax.inject.Inject
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.of

public abstract class GitVersionExtensionReadonly @Inject constructor(
    private val providers: ProviderFactory,
) {

    public abstract val tagPrefix: Provider<String>

    public abstract val forceSnapshot: Provider<Boolean>

    public abstract val initialVersion: Provider<String>

    public abstract val version: Provider<String>

    public abstract val versionModifier: Provider<String>

    public abstract val forWholeBuild: Provider<Boolean>

    public fun provider(producer: GitVersionProducer?): Provider<String> = providers.of(GitVersionValueSource::class) {
        parameters.tagPrefix.set(tagPrefix)
        parameters.initialVersion.set(initialVersion)
        parameters.forceSnapshot.set(forceSnapshot)
        parameters.versionModifier.set(versionModifier)
        parameters.versionProducer.set(producer)
    }

    final override fun toString(): String =
        version.get().toString()

}
