package io.github.gmazzo.gitversion

import javax.inject.Inject
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.internal.GradleInternal
import org.gradle.api.invocation.Gradle
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.newInstance

class GitVersionPlugin @Inject constructor(
    private val objects: ObjectFactory,
    private val providers: ProviderFactory,
) : Plugin<Any> {

    override fun apply(target: Any): Unit = when (target) {
        is Project -> target.configure(target::allprojects)
        is Settings -> target.configure(target.gradle::allprojects)
        is Gradle -> target.configure(target::allprojects)
        else -> throw IllegalArgumentException("Unsupported target object: $target")
    }

    private fun ExtensionAware.configure(onEachProject: (Action<Project>) -> Unit) {
        val extension = findOrCreateExtension extension@{

            tagPrefix
                .convention("v")
                .finalizeValueOnRead()

            initialVersion
                .convention("0.1.0-SNAPSHOT")
                .finalizeValueOnRead()

            forceSnapshot
                .convention(providers.gradleProperty("gitVersionForceSnapshot").map { it.toBoolean() }.orElse(false))
                .finalizeValueOnRead()

            versionModifier
                .convention(providers.gradleProperty("gitVersionModifier").orElse(""))
                .finalizeValueOnRead()

            versionProducer
                .finalizeValueOnRead()

            version
                .convention(versionProducer.flatMap(::provider).orElse(provider(null)))
                .finalizeValueOnRead()

            forWholeBuild
                .convention(if (this@configure is Project) project == rootProject else true)
                .finalizeValueOnRead()

        }

        propagateExtension(extension)

        onEachProject {
            version = extension
        }
    }

    /**
     * We work with root's `Gradle` object, to allow providing a consistent extension object
     * between main and any included builds.
     */
    private fun ExtensionAware.findOrCreateExtension(
        onCreate: GitVersionExtension.() -> Unit,
    ): GitVersionExtensionReadonly = when (val existing = findExtensionOnBuildHierarchy()) {
        null -> extensions.create<GitVersionExtension>(EXTENSION_NAME).also(onCreate)
        else -> existing.also { extensions.add(EXTENSION_NAME, it) }
    }

    private fun ExtensionAware.findExtensionOnBuildHierarchy() = generateSequence(parent) { it.parent }
        .mapNotNull { it.extensions.findByName(EXTENSION_NAME) }
        .map { objects.newInstance<GitVersionExtensionReflected>(providers, objects, it) }
        .firstOrNull()

    private val ExtensionAware.parent: ExtensionAware?
        get() = when (this) {
            is Project -> parent ?: gradle
            is Settings -> gradle
            is Gradle -> parent
            else -> throw IllegalArgumentException("Unsupported target object: $this")
        }

    private fun ExtensionAware.propagateExtension(extension: GitVersionExtensionReadonly) {
        val propagate by lazy { extension.forWholeBuild.getOrElse(true) }

        when (this) {
            is Project -> afterEvaluate { if (propagate) gradle.propagate(extension) }
            is Settings -> gradle.settingsEvaluated { if (propagate) gradle.propagate(extension) }
        }
    }

    private fun Gradle.propagate(extension: GitVersionExtensionReadonly) = with((this as GradleInternal).root) {
        extensions.findByName(EXTENSION_NAME) // it may exist already, even from another classpath
            ?: extensions.add(EXTENSION_NAME, extension)
    }

    companion object {
        const val EXTENSION_NAME = "gitVersion"
    }

}
