package io.github.gmazzo.gitversion

import javax.inject.Inject
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.initialization.Settings
import org.gradle.api.internal.GradleInternal
import org.gradle.api.invocation.Gradle
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.typeOf

class GitVersionPlugin @Inject constructor(
    private val providers: ProviderFactory,
) : Plugin<Any> {

    override fun apply(target: Any) = when (target) {
        is Project -> target.configure(target::allprojects)
        is Settings -> target.configure(target.gradle::allprojects)
        is Gradle -> target.configure(target::allprojects)
        else -> throw IllegalArgumentException("Unsupported target object: $target")
    }

    private fun ExtensionAware.configure(onEachProject: (Action<Project>) -> Unit) {
        val extension = findOrCreateExtension {

            gitRootDirectory
                .convention(gitRoot)
                .finalizeValueOnRead()

            tagPrefix
                .convention("v")
                .finalizeValueOnRead()

            versionProducer
                .convention(GitVersionRegexBasedProducer.Snapshot)
                .finalizeValueOnRead()

            val command = gitRootDirectory.zip(tagPrefix.orElse(""), ::Pair).flatMap { (rootDir, prefix) ->
                rootDir.command("git", "describe", "--tags", "--always", "--match", "$prefix*")
            }

            version
                .convention(versionProducer.zip(command, GitVersionProducer::versionFor))
                .finalizeValueOnRead()

        }

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
    ): GitVersionExtension = when (val existing = findExtensionOnBuildHierarchy()) {
        null -> extensions.create<GitVersionExtension>("gitVersion").also(onCreate)
        else -> existing.also { extensions.add(typeOf<GitVersionExtension>(), "gitVersion", it) }
    }

    private fun ExtensionAware.findExtensionOnBuildHierarchy() = generateSequence(this) { it.parent }
        .mapNotNull { it.extensions.findByName("gitVersion") }
        .map { it as? GitVersionExtension ?: GitVersionExtensionReflected(it) }
        .firstOrNull()

    private val ExtensionAware.parent: ExtensionAware?
        get() = when (this) {
            is Project -> parent ?: gradle
            is Settings -> gradle
            is Gradle -> parent
            else -> throw IllegalArgumentException("Unsupported target object: $this")
        }

    @Suppress("UnstableApiUsage")
    private val ExtensionAware.gitRoot: Directory
        get() = when (this) {
            is Project -> gradle.gitRoot
            is Settings -> layout.rootDirectory
            is Gradle -> (this as GradleInternal).settings.gitRoot
            else -> throw IllegalArgumentException("Unsupported target object: $this")
        }

    private fun Directory.command(vararg commandLine: String) = providers
        .exec {
            workingDir = this@command.asFile
            commandLine(*commandLine)
            isIgnoreExitValue = true
        }
        .let { exec ->
            exec.result
                .map { if (it.exitValue == 0) exec.standardOutput else exec.standardError }
                .map { it.asText.get().trim() }
        }

}
