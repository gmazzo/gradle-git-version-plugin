package io.github.gmazzo.gitversion

import java.io.File
import org.gradle.kotlin.dsl.of
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GitVersionValueSourceTest {
    private val tempDir = File(System.getenv("TEMP_DIR"), "valueSourceRepo")

    private val project = ProjectBuilder.builder().withProjectDir(tempDir).build()

    @BeforeAll
    fun setup() = with(tempDir) {
        deleteRecursively()
        mkdirs()
        command("git", "init")
        command("git", "config", "user.email", "test@test.org")
        command("git", "config", "user.name", "test")

        command("git", "commit", "--allow-empty", "-m", "Commit #1")
        command("git", "tag", "foo-1.0.0")

        command("git", "commit", "--allow-empty", "-m", "Commit #2")
        command("git", "tag", "bar-2.0")

        command("git", "commit", "--allow-empty", "-m", "Commit #3")
        command("git", "commit", "--allow-empty", "-m", "Commit #4")
        command("git", "tag", "foo-1.1.0")

        command("git", "commit", "--allow-empty", "-m", "Commit #5")
        command("git", "tag", "foo-1.2.0")

        command("git", "commit", "--allow-empty", "-m", "Commit #6")
        command("git", "tag", "bar-3.0")

        command("git", "commit", "--allow-empty", "-m", "Commit #7")
        command("git", "tag", "foo-2.0.0")
        command("git", "commit", "--allow-empty", "-m", "Commit #8")
        command("git", "tag", "bar-3.1")
    }

    @ParameterizedTest(name = "tagPrefix={0}")
    @MethodSource("versionsData")
    fun `obtains returns the default candidate`(
        tagPrefix: String,
        forceSnapshot: Boolean,
        expectedVersion: String,
    ) = with(project) {
        val source = providers.of(GitVersionValueSource::class) {
            parameters.gitRoot.set(tempDir)
            parameters.tagPrefix.set(tagPrefix)
            parameters.forceSnapshot.set(forceSnapshot)
            parameters.initialVersion.set("0.1.0-initial-SNAPSHOT")
        }

        assertEquals(expectedVersion, source.get())
    }

    fun versionsData(): List<Array<*>> = listOf(
        arrayOf<Any>("foo-", false, "2.1.0-SNAPSHOT"),
        arrayOf<Any>("foo-", true, "2.1.0-SNAPSHOT"),
        arrayOf<Any>("bar-", false, "3.1"),
        arrayOf<Any>("bar-", true, "3.1-SNAPSHOT"),
    )

    @ParameterizedTest(name = "tagPrefix={0}")
    @MethodSource("tagCountsData")
    fun `tags count is the expected`(
        tagPrefix: String,
        expectedCount: Int,
    ) = with(project) {
        val source = providers.of(GitVersionValueSource::class) {
            parameters.gitRoot.set(tempDir)
            parameters.tagPrefix.set(tagPrefix)
            parameters.forceSnapshot.set(false)
            parameters.initialVersion.set("0.1.0-initial-SNAPSHOT")
            parameters.versionProducer.set { tagsCount().toString() }
        }

        assertEquals(expectedCount, source.get().toInt())
    }

    fun tagCountsData(): List<Array<*>> = listOf(
        arrayOf<Any>("foo-", 4),
        arrayOf<Any>("bar-", 3),
    )

    @AfterAll
    fun cleanup() {
        tempDir.resolve(".git").deleteRecursively()
    }

}
