package io.github.gmazzo.gitversion

import java.io.ByteArrayOutputStream
import javax.inject.Inject
import org.gradle.api.file.DirectoryProperty
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
abstract class GitVersionValueSource @Inject constructor(
    private val execOperations: ExecOperations,
) : ValueSource<String, GitVersionValueSource.Params> {
    private val logger = Logging.getLogger(GitVersionValueSource::class.java)

    private val prefix by lazy { parameters.tagPrefix.getOrElse("") }

    val modifier by lazy { GitVersionModifierSpec.parse(parameters.versionModifier.get()) }

    val candidate: String by lazy {
        with(parameters) {
            var snapshot = forceSnapshot.get()
            val tag = exactTag() ?: closestTag()?.also { snapshot = true }
            val version = tag?.removePrefix(prefix) ?: initialVersion.get()

            modifier.let {
                if (snapshot) it.copy(
                    bump = it.bump ?: GitVersionModifierSpec.Bump.MINOR,
                    labels = it.labels.orEmpty() + "SNAPSHOT",
                ) else it
            }.modify(version)
        }
    }

    override fun obtain(): String {
        val version = parameters.versionProducer.orNull?.produceVersion(this) ?: candidate
        if (modifier.storeTag) {
            command("git", "tag", "$prefix$version") {
                logger.error("Failed to store git tag '$version': $it")
            }
        }
        return version
    }


    fun exactTag(tagPrefix: String = prefix) =
        command("git", "describe", "--tags", "--match", "$tagPrefix*", "--exact-match")

    fun closestTag(tagPrefix: String = prefix, warnIfMissing: Boolean = true) =
        command("git", "describe", "--tags", "--match", "$tagPrefix*", "--abbrev=0") {
            if (warnIfMissing) {
                logger.warn("failed to compute git version (no tags yet?): $it")
            }
        }

    fun tagsCount(tagPrefix: String = prefix): Int =
        command("git", "tag", "--merged", "HEAD", "$tagPrefix*")!!.lines().count()

    fun command(vararg args: String, onError: (String) -> Unit = {}): String? {
        val stdout = ByteArrayOutputStream()
        val stderr = ByteArrayOutputStream()
        val exitValue = execOperations.exec {
            parameters.gitRoot.asFile.orNull?.let(::workingDir)
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

    interface Params : ValueSourceParameters {

        val gitRoot: DirectoryProperty

        val tagPrefix: Property<String>

        val initialVersion: Property<String>

        val forceSnapshot: Property<Boolean>

        val versionModifier: Property<String>

        val versionProducer: Property<GitVersionProducer>

    }

}
