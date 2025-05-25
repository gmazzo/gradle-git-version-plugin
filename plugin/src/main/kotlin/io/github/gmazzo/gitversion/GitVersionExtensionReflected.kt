package io.github.gmazzo.gitversion

import javax.inject.Inject
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaMethod
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.property

/**
 * Allows accessing the root [GitVersionExtension] extension, when the plugin is loaded in different classloaders
 * (mostly when using [org.gradle.api.initialization.Settings.includeBuild]).
 */
@Suppress("UNCHECKED_CAST")
internal abstract class GitVersionExtensionReflected @Inject constructor(
    providers: ProviderFactory,
    private val objects: ObjectFactory,
    private val delegate: Any,
) : GitVersionExtensionReadonly(providers) {

    override val tagPrefix = delegated(GitVersionExtension::tagPrefix)

    override val initialVersion = delegated(GitVersionExtension::initialVersion)

    override val forceSnapshot = delegated(GitVersionExtension::forceSnapshot)

    override val version = delegated(GitVersionExtension::version)

    override val forWholeBuild = delegated(GitVersionExtension::forWholeBuild)

    private inline fun <reified Type> delegated(
        property: KProperty<Provider<Type>>,
    ): Property<Type> = try {
        delegate.javaClass
            .getMethod(property.getter.javaMethod!!.name)
            .invoke(delegate) as Property<Type>

    } catch (e: NoSuchMethodException) {
        e.printStackTrace() // this may happen if different binary incompatible versions are loaded
        objects.property<Type>()
    }

}
