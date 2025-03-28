package io.github.gmazzo.gitversion

import java.io.ByteArrayOutputStream
import javax.inject.Inject
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.process.ExecOperations

/**
 * A [ValueSource] that computes the version based on the latest git tag (optional filtering by [GitVersionTagBasedValueSource.Params.tagPrefix]).
 *
 * The version is computed as follows:
 * 1) If the current commit is tagged, the version is the tag name.
 * 2) If the current commit is not tagged, the version is the tag name of the latest tag reachable from the current commit suffixed with `-SNAPSHOT`.
 * 3) If there are no tags, the version is `0.1.0-SNAPSHOT` (configured through [GitVersionTagBasedValueSource.Params.initialVersion]).
 */
abstract class GitVersionTagBasedValueSource @Inject constructor(
    private val execOperations: ExecOperations,
) : ValueSource<String, GitVersionTagBasedValueSource.Params> {
    private val logger = Logging.getLogger(GitVersionTagBasedValueSource::class.java)

    override fun obtain(): String = with(parameters) {
        val prefix = tagPrefix.getOrElse("")
        val tag = command("git", "describe", "--tags", "--match", "$prefix*", "--exact-match")
            ?: command("git", "describe", "--tags", "--match", "$prefix*", "--abbrev=0") {
                logger.warn("failed to compute git version (no tags yet?): $it")
            }?.let { "$it$SNAPSHOT_SUFFIX" }

        val candidate = tag?.removePrefix(prefix) ?: parameters.initialVersion.get()
        if (forceSnapshot.get() && !candidate.endsWith(SNAPSHOT_SUFFIX)) "$candidate$SNAPSHOT_SUFFIX" else candidate
    }

    protected fun command(vararg args: String, onError: (String) -> Unit = {}): String? {
        val stdout = ByteArrayOutputStream()
        val stderr = ByteArrayOutputStream()
        val exitValue = execOperations.exec {
            commandLine = args.toList()
            isIgnoreExitValue = true
            standardOutput = stdout
            errorOutput = stderr
        }.exitValue

        if (exitValue == 0) {
            return stdout.toString().trim()
        }
        onError(stderr.toString().trim())
        return null
    }

    @JvmDefaultWithoutCompatibility
    interface Params : ValueSourceParameters {

        val tagPrefix: Property<String>

        val initialVersion: Property<String>

        val forceSnapshot: Property<Boolean>

        fun from(extension: GitVersionExtension) = apply {
            tagPrefix.set(extension.tagPrefix)
            initialVersion.set(extension.initialVersion)
        }

    }

    companion object {
        const val SNAPSHOT_SUFFIX = "-SNAPSHOT"
    }

}
