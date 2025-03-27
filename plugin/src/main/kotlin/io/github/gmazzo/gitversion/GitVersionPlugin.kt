package io.github.gmazzo.gitversion

import javax.inject.Inject
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.initialization.Settings
import org.gradle.api.internal.GradleInternal
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.typeOf

class GitVersionPlugin @Inject constructor(
    private val providers: ProviderFactory,
) : Plugin<Any> {

    private val logger = Logging.getLogger(GitVersionPlugin::class.java)

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


            version
                .convention(tagBasedVersion())
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

    private fun GitVersionExtension.tagBasedVersion() = gitRootDirectory
        .zip(tagPrefix.orElse(""), ::Pair)
        .flatMap { (rootDir, prefix) ->
            with(rootDir) {
                val baseCommand = arrayOf("git", "describe", "--tags", "--match", "$prefix*")

                command(baseCommand, "--exact-match") { null }
                    .orElse(command(baseCommand, "--abbrev=0") {
                        logger.warn("failed to compute git version (no tags yet?): $it")
                        null
                    }.map { "$it-SNAPSHOT" })
                    .map { it.removePrefix(prefix) }
                    .orElse("0.1.0-SNAPSHOT")
            }
        }

    private fun Directory.command(
        commandLine: Array<String>,
        vararg args: String,
        onError: (String) -> String?,
    ) = providers.exec {
        workingDir = this@command.asFile
        commandLine(commandLine.asList() + args)
        isIgnoreExitValue = true
    }.let { exec ->
        exec.result.map {
            when (it.exitValue) {
                0 -> exec.standardOutput.asText.get().trim()
                else -> onError(exec.standardError.asText.get().trim())
            }
        }
    }
}
