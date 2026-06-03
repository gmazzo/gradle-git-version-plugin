package io.github.gmazzo.gitversion

import javax.inject.Inject
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.of

public interface GitVersionExtensionReadonly {

    public val tagPrefix: Provider<String>

    public val forceSnapshot: Provider<Boolean>

    public val initialVersion: Provider<String>

    public val version: Provider<String>

    public val versionModifier: Provider<String>

    public val forWholeBuild: Provider<Boolean>

    public fun provider(producer: GitVersionProducer?): Provider<String>

}
