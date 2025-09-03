package io.github.gmazzo.gitversion

import java.io.File
import org.gradle.kotlin.dsl.of
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GitVersionValueSourceTest {
    private val tempDir = File(System.getenv("TEMP_DIR"), "valueSourceRepo")

    private val project = ProjectBuilder.builder().withProjectDir(tempDir).build()

    @BeforeAll
    fun setup(): Unit = with(tempDir) {
        deleteRecursively()
        mkdirs()
        command("git", "init")
        command("git", "config", "user.email", "test@test.org")
        command("git", "config", "user.name", "test")

        command("git", "commit", "--allow-empty", "-m", "Commit #1")
        command("git", "tag", "foo-1.0.0")

        command("git", "commit", "--allow-empty", "-m", "Commit #2")
        command("git", "tag", "bar-2.0.5")

        command("git", "commit", "--allow-empty", "-m", "Commit #3")
        command("git", "commit", "--allow-empty", "-m", "Commit #4")
        command("git", "tag", "foo-1.1.0")

        command("git", "commit", "--allow-empty", "-m", "Commit #5")
        command("git", "tag", "foo-1.2.0")

        command("git", "commit", "--allow-empty", "-m", "Commit #6")
        command("git", "tag", "bar-3.0.6")

        command("git", "commit", "--allow-empty", "-m", "Commit #7")
        command("git", "tag", "foo-2.0.0")
        command("git", "commit", "--allow-empty", "-m", "Commit #8")
        command("git", "tag", "bar-3.1.0")
    }

    @AfterEach
    fun cleanupNewTags(): Unit = with(tempDir) {
        for (tag in command("git", "tag", "--points-at", "HEAD").lines()) {
            if (tag != "bar-3.1.0") {
                command("git", "tag", "-d", tag)
            }
        }
    }

    @AfterAll
    fun cleanup() {
        tempDir.resolve(".git").deleteRecursively()
    }

    @ParameterizedTest(name = "tagPrefix={0}, forceSnapshot={1}, versionModifier={2} => {3}")
    @MethodSource("versionsData")
    fun `obtains returns the default candidate`(
        tagPrefix: String,
        forceSnapshot: Boolean,
        versionModifier: String,
        expectedVersion: String,
        expectedToStoreTag: Boolean,
    ) = with(project) {
        val expectedHeadTag = "$tagPrefix$expectedVersion"

        val actionVersion = providers.of(GitVersionValueSource::class) {
            parameters.gitRoot.set(tempDir)
            parameters.tagPrefix.set(tagPrefix)
            parameters.forceSnapshot.set(forceSnapshot)
            parameters.versionModifier.set(versionModifier)
            parameters.initialVersion.set("0.1.0-initial-SNAPSHOT")
        }.get()

        val headTag = tempDir
            .command("git", "tag", "--points-at", "HEAD")
            .lines()
            .let { it.find { t -> t == expectedHeadTag } ?: it.firstOrNull() }

        assertEquals(expectedVersion, actionVersion)
        if (expectedToStoreTag) assertEquals(expectedHeadTag, headTag)
        else assertNotEquals(expectedHeadTag, headTag)
    }

    fun versionsData(): List<Array<*>> = listOf(
        arrayOf<Any>("foo-", false, "label=beta,label=rc-1,storeTag", "2.1.0-beta-rc-1-SNAPSHOT", true),
        arrayOf<Any>("foo-", true, "label=beta,label=rc-1,storeTag", "2.1.0-beta-rc-1-SNAPSHOT", true),
        arrayOf<Any>("bar-", false, "", "3.1.0", true /* this is the latest one */),
        arrayOf<Any>("bar-", true, "", "3.2.0-SNAPSHOT", false),
        arrayOf<Any>("bar-", false, "major", "4.0.0", false),
        arrayOf<Any>("bar-", true, "major", "4.0.0-SNAPSHOT", false),
        arrayOf<Any>("bar-", false, "minor", "3.2.0", false),
        arrayOf<Any>("bar-", true, "minor", "3.2.0-SNAPSHOT", false),
        arrayOf<Any>("bar-", false, "patch", "3.1.1", false),
        arrayOf<Any>("bar-", true, "patch", "3.1.1-SNAPSHOT", false),
        arrayOf<Any>("bar-", false, "patch,storeTag", "3.1.1", true),
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
            parameters.versionModifier.set("")
            parameters.versionProducer.set { tagsCount().toString() }
        }

        assertEquals(expectedCount, source.get().toInt())
    }

    fun tagCountsData(): List<Array<*>> = listOf(
        arrayOf<Any>("foo-", 4),
        arrayOf<Any>("bar-", 3),
    )

}
