package io.github.gmazzo.gitversion

import io.github.gmazzo.gitversion.GitVersionValueSource.Companion.SNAPSHOT_SUFFIX
import javax.inject.Inject
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.internal.GradleInternal
import org.gradle.api.invocation.Gradle
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.of

class GitVersionPlugin @Inject constructor(
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
                .convention("0.1.0$SNAPSHOT_SUFFIX")
                .finalizeValueOnRead()

            forceSnapshot
                .convention(providers.gradleProperty("gitVersionForceSnapshot").map(String::toBoolean).orElse(false))
                .finalizeValueOnRead()

            versionProducer
                .finalizeValueOnRead()

            version
                .convention(providers.of(GitVersionValueSource::class) {
                    parameters.tagPrefix.set(tagPrefix)
                    parameters.forceSnapshot.set(forceSnapshot)
                    parameters.initialVersion.set(initialVersion)
                    parameters.versionProducer.set(versionProducer)
                })
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
        onCreate: GitVersionExtensionInternal.() -> Unit,
    ): GitVersionExtension = when (val existing = findExtensionOnBuildHierarchy()) {
        null -> createExtension().also { onCreate(it as GitVersionExtensionInternal) }
        else -> existing.also { extensions.add(EXTENSION_NAME, it) }
    }

    private fun ExtensionAware.findExtensionOnBuildHierarchy() = generateSequence(this) { it.parent }
        .mapNotNull { it.extensions.findByName(EXTENSION_NAME) }
        .map { it as? GitVersionExtension ?: GitVersionExtensionReadonly(it) }
        .firstOrNull()

    private fun ExtensionAware.createExtension() =
        extensions.create(GitVersionExtension::class, EXTENSION_NAME, GitVersionExtensionInternal::class, "$this")

    private val ExtensionAware.parent: ExtensionAware?
        get() = when (this) {
            is Project -> parent ?: gradle
            is Settings -> gradle
            is Gradle -> parent
            else -> throw IllegalArgumentException("Unsupported target object: $this")
        }

    private fun ExtensionAware.propagateExtension(extension: GitVersionExtension) {
        fun Gradle.propagate() = with((this as GradleInternal).root) {
            extensions.findByName(EXTENSION_NAME) // it may exist already, even from another classpath
                ?: extensions.add(EXTENSION_NAME, extension)
        }

        when (this) {
            is Project -> if (project == rootProject) gradle.propagate()
            is Settings -> gradle.propagate()
        }
    }

    companion object {
        const val EXTENSION_NAME = "gitVersion"
    }

}
