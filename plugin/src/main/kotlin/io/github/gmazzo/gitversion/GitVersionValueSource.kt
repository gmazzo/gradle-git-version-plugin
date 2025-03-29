package io.github.gmazzo.gitversion

import java.io.ByteArrayOutputStream
import javax.inject.Inject
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.process.ExecOperations

/**
 * A [ValueSource] that computes the version based on the latest git tag (optional filtering by [GitVersionValueSource.Params.tagPrefix]).
 *
 * The version is computed as follows:
 * 1) If the current commit is tagged, the version is the tag name.
 * 2) If the current commit is not tagged, the version is the tag name of the latest tag reachable from the current commit suffixed with `-SNAPSHOT`.
 * 3) If there are no tags, the version is `0.1.0-SNAPSHOT` (configured through [GitVersionValueSource.Params.initialVersion]).
 */
abstract class GitVersionValueSource : ValueSource<String, GitVersionValueSource.Params> {
    private val logger = Logging.getLogger(GitVersionValueSource::class.java)

    @get:Inject
    protected abstract val execOperations: ExecOperations

    override fun obtain(): String = with(parameters) {
        var snapshot = forceSnapshot.get()
        val prefix = tagPrefix.getOrElse("")
        val tag = exactTag() ?: closestTag()?.also { snapshot = true }
        val version = tag?.removePrefix(prefix) ?: parameters.initialVersion.get()

        if (snapshot) version.nextSnapshot else version
    }

    protected fun exactTag(tagPrefix: String = parameters.tagPrefix.getOrElse("")) =
        command("git", "describe", "--tags", "--match", "$tagPrefix*", "--exact-match")

    protected fun closestTag(tagPrefix: String = parameters.tagPrefix.getOrElse(""), warnIfMissing: Boolean = true) =
        command("git", "describe", "--tags", "--match", "$tagPrefix*", "--abbrev=0") {
            if (warnIfMissing) {
                logger.warn("failed to compute git version (no tags yet?): $it")
            }
        }

    protected val String.nextSnapshot: String
        get() = when (val match = "(\\d+)\\.(\\d+)\\.(\\d+)(?:\\.(\\d+))?".toRegex().matchEntire(this)) {
            null -> if (endsWith(SNAPSHOT_SUFFIX)) this else "$this$SNAPSHOT_SUFFIX"
            else -> match.groupValues.asSequence()
                .drop(1)
                .filter { it.isNotBlank() }
                .mapIndexed { i, it ->
                    when (i) {
                        0 -> it
                        1 -> it.toInt() + 1
                        else -> 0
                    }
                }
                .joinToString(".") + SNAPSHOT_SUFFIX
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

    }

    companion object {
        const val SNAPSHOT_SUFFIX = "-SNAPSHOT"
    }

}
