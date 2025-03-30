@file:Suppress("EnumEntryName")

package io.github.gmazzo.gitversion

import java.io.File
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GitVersionPluginTest {
    private val gitVersionPlugin = "io.github.gmazzo.gitversion"
    private val tempDir = File(System.getenv("TEMP_DIR"), "project")

    @BeforeAll
    fun setup() {
        tempDir.deleteRecursively()
    }

    @ParameterizedTest(name = "{0}, {1}, {2}")
    @MethodSource("testData")
    fun `plugin can be applied in a simple project`(layout: BuildLayout, applyAt: ApplyAt, tags: Tags) =
        runTest(layout, applyAt, tags) {
            val expectedVersion = when (applyAt) {
                ApplyAt.childProjects -> "unspecified"
                else -> when (tags) {
                    Tags.noneMatching -> "0.1.0-SNAPSHOT"
                    Tags.previousInHistory -> "1.3.0-SNAPSHOT"
                    Tags.isHead -> "2.0.5"
                }
            }

            val expectedVersions = when (layout) {
                BuildLayout.singleModule -> mapOf(
                    "version.txt" to expectedVersion
                )

                BuildLayout.multiModule -> when (applyAt) {
                    ApplyAt.childProjects -> mapOf(
                        "version.txt" to "unspecified",
                        "module1/version.txt" to "1.1.0-SNAPSHOT",
                        "module2/version.txt" to "2.0.0",
                    )

                    else -> mapOf(
                        "version.txt" to expectedVersion,
                        "module1/version.txt" to expectedVersion,
                        "module2/version.txt" to expectedVersion,
                    )
                }

                BuildLayout.includedBuild -> when (applyAt) {
                    ApplyAt.childProjects -> mapOf(
                        "version.txt" to "unspecified",
                        "module1/version.txt" to "1.1.0-SNAPSHOT",
                        "module2/version.txt" to "2.0.0",
                        "build-logic/version.txt" to "unspecified",
                    )

                    else -> mapOf(
                        "version.txt" to expectedVersion,
                        "module1/version.txt" to expectedVersion,
                        "module2/version.txt" to expectedVersion,
                        "build-logic/version.txt" to expectedVersion,
                    )
                }
            }

            GradleRunner.create()
                .withPluginClasspath()
                .withProjectDir(rootDir)
                .withArguments(
                    listOfNotNull(
                        "-s",
                        "storeVersion",
                        "build-logic:storeVersion".takeIf { layout == BuildLayout.includedBuild }),
                )
                .forwardOutput()
                .build()

            val actualVersions = rootDir.walkTopDown()
                .filter { it.name == "version.txt" }
                .associateBy({ it.toRelativeString(rootDir) }, { it.readText() })

            assertEquals(expectedVersions, actualVersions)
        }

    private fun runTest(layout: BuildLayout, applyAt: ApplyAt, tags: Tags, block: Scenario.() -> Unit) {
        val rootDir = tempDir.resolve(layout.name).resolve(applyAt.name).resolve(tags.name)

        rootDir.mkdirs()
        rootDir.command("git", "init")
        rootDir.command("git", "config", "user.email", "test@test.org")
        rootDir.command("git", "config", "user.name", "test")
        rootDir.command("git", "commit", "--allow-empty", "-m", "Initial commit")
        rootDir.command("git", "commit", "--allow-empty", "-m", "Second commit")
        rootDir.command("git", "tag", "module1-v1.0.0")
        if (tags == Tags.previousInHistory) rootDir.command("git", "tag", "v1.2.3")
        rootDir.command("git", "commit", "--allow-empty", "-m", "Third commit")
        rootDir.command("git", "commit", "--allow-empty", "-m", "Fourth commit")
        if (tags == Tags.isHead) rootDir.command("git", "tag", "v2.0.5")
        rootDir.command("git", "tag", "module2-v2.0.0")

        rootDir.resolve("settings.gradle").apply {
            if (applyAt == ApplyAt.settings) writeText(
                """
                    plugins {
                        id("$gitVersionPlugin")
                        id("jacoco-testkit-coverage")
                    }

                    """.trimIndent()
            )
            else createNewFile()

            appendText("rootProject.name = \"test\"\n\n")

            if (layout == BuildLayout.includedBuild) appendText("includeBuild(\"build-logic\")\n")
            if (layout == BuildLayout.multiModule || layout == BuildLayout.includedBuild)
                appendText("include(\"module1\", \"module2\")\n")
        }

        rootDir.resolve("build.gradle").apply {
            if (applyAt == ApplyAt.rootProject) writeText("plugins { id(\"$gitVersionPlugin\") }\n\n")
            else createNewFile()
            addStoreVersionTask()
        }

        if (layout == BuildLayout.includedBuild) {
            rootDir.resolve("build-logic/settings.gradle").apply {
                parentFile.mkdirs()
                if (applyAt == ApplyAt.settings) writeText(
                    """
                    plugins {
                        id("$gitVersionPlugin")
                        id("jacoco-testkit-coverage")
                    }

                    """.trimIndent()
                )
                else createNewFile()

                appendText("rootProject.name = \"build-logic\"\n\n")
            }
            rootDir.resolve("build-logic/build.gradle").apply {
                if (applyAt == ApplyAt.rootProject) writeText("plugins { id(\"$gitVersionPlugin\") }\n\n")
                else createNewFile()
                addStoreVersionTask()
            }
        }

        if (layout == BuildLayout.multiModule || layout == BuildLayout.includedBuild) {
            rootDir.resolve("module1/build.gradle").apply {
                parentFile.mkdirs()
                if (applyAt == ApplyAt.childProjects) writeText(
                    """
                    plugins { id("$gitVersionPlugin") }
                    gitVersion.tagPrefix = "module1-v"

                    """.trimIndent()
                )
                else createNewFile()
            }

            rootDir.resolve("module2/build.gradle").apply {
                parentFile.mkdirs()
                if (applyAt == ApplyAt.childProjects) writeText(
                    """
                    plugins { id("$gitVersionPlugin") }
                    gitVersion.tagPrefix = "module2-v"

                    """.trimIndent()
                )
                else createNewFile()
            }
        }

        Scenario(
            layout = layout,
            applyTarget = applyAt,
            tags = tags,
            rootDir = rootDir,
        ).block()
    }

    private fun File.command(vararg commandLine: String) =
        with(Runtime.getRuntime().exec(commandLine, null, this)) {
            check(waitFor() == 0) {
                "Command '${commandLine.joinToString(" ")}' failed: ${
                    errorStream.reader().readText().trim()
                }"
            }
        }

    private fun File.addStoreVersionTask() {
        appendText(
            """
            allprojects {
                tasks.register("storeVersion") {
                    def versionFile = file("version.txt")
                    def version = "${'$'}{project.version}"
                    doLast {
                        versionFile.text = version
                    }
                }
            }

            """.trimIndent()
        )
    }

    fun testData(): List<Array<*>> = BuildLayout.entries.flatMap { layout ->
        ApplyAt.entries.flatMap { applyAt ->
            Tags.entries.map { tags -> arrayOf(layout, applyAt, tags) }
        }
    }

    data class Scenario(
        val layout: BuildLayout,
        val applyTarget: ApplyAt,
        val tags: Tags,
        val rootDir: File,
    )

    enum class BuildLayout {
        singleModule,
        multiModule,
        includedBuild
    }

    enum class ApplyAt {
        rootProject,
        settings,
        childProjects,
    }

    enum class Tags {
        noneMatching,
        previousInHistory,
        isHead,
    }

}
