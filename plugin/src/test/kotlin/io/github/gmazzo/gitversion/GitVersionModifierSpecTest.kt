package io.github.gmazzo.gitversion

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GitVersionModifierSpecTest {

    @ParameterizedTest(name = "{0} => {1}")
    @MethodSource("parseTestData")
    fun parseTest(input: String, expectedSpec: GitVersionModifierSpec) {
        val actualSpec = GitVersionModifierSpec.parse(input)

        assertEquals(expectedSpec, actualSpec)
        assertEquals(input, actualSpec.toString())
    }

    fun parseTestData() = listOf(
        arrayOf("major", GitVersionModifierSpec(bump = GitVersionModifierSpec.Bump.MAJOR)),
        arrayOf("minor", GitVersionModifierSpec(bump = GitVersionModifierSpec.Bump.MINOR)),
        arrayOf("patch", GitVersionModifierSpec(bump = GitVersionModifierSpec.Bump.PATCH)),
        arrayOf("build", GitVersionModifierSpec(bump = GitVersionModifierSpec.Bump.BUILD)),
        arrayOf("label=", GitVersionModifierSpec(labels = emptySet())),
        arrayOf("label=beta", GitVersionModifierSpec(labels = setOf("beta"))),
        arrayOf("label=beta,label=rc.1", GitVersionModifierSpec(labels = setOf("beta", "rc.1"))),
        arrayOf("metadata=", GitVersionModifierSpec(metadata = emptySet())),
        arrayOf("metadata=001", GitVersionModifierSpec(metadata = setOf("001"))),
        arrayOf(
            "metadata=exp.sha.5114f85,metadata=build.1",
            GitVersionModifierSpec(metadata = setOf("exp.sha.5114f85", "build.1"))
        ),
        arrayOf("storeTag", GitVersionModifierSpec(storeTag = true)),
        arrayOf(
            "major,label=beta,metadata=001,storeTag",
            GitVersionModifierSpec(
                bump = GitVersionModifierSpec.Bump.MAJOR,
                labels = setOf("beta"),
                metadata = setOf("001"),
                storeTag = true
            )
        ),
    )

    @ParameterizedTest(name = "{0}, modifyWith: {1} => {2}")
    @MethodSource("modifyTestData")
    fun modifyTest(version: String, spec: GitVersionModifierSpec, expectedVersion: String) {
        val actualVersion = spec.modify(version)

        assertEquals(expectedVersion, actualVersion)
    }

    fun modifyTestData() = listOf(
        arrayOf("1.2", GitVersionModifierSpec(), "1.2"),
        arrayOf("1.2", GitVersionModifierSpec(bump = GitVersionModifierSpec.Bump.MAJOR), "2.0"),
        arrayOf("1.2", GitVersionModifierSpec(bump = GitVersionModifierSpec.Bump.MINOR), "1.3"),
        arrayOf("1.2", GitVersionModifierSpec(bump = GitVersionModifierSpec.Bump.PATCH), "1.2.1"),
        arrayOf("1.2", GitVersionModifierSpec(bump = GitVersionModifierSpec.Bump.BUILD), "1.2.0.1"),
        arrayOf("1.2.3", GitVersionModifierSpec(), "1.2.3"),
        arrayOf("1.2.3", GitVersionModifierSpec(bump = GitVersionModifierSpec.Bump.MAJOR), "2.0.0"),
        arrayOf("1.2.3", GitVersionModifierSpec(bump = GitVersionModifierSpec.Bump.MINOR), "1.3.0"),
        arrayOf("1.2.3", GitVersionModifierSpec(bump = GitVersionModifierSpec.Bump.PATCH), "1.2.4"),
        arrayOf("1.2.3", GitVersionModifierSpec(bump = GitVersionModifierSpec.Bump.BUILD), "1.2.3.1"),
        arrayOf("1.2.3.4", GitVersionModifierSpec(), "1.2.3.4"),
        arrayOf("1.2.3.4", GitVersionModifierSpec(bump = GitVersionModifierSpec.Bump.MAJOR), "2.0.0.0"),
        arrayOf("1.2.3.4", GitVersionModifierSpec(bump = GitVersionModifierSpec.Bump.MINOR), "1.3.0.0"),
        arrayOf("1.2.3.4", GitVersionModifierSpec(bump = GitVersionModifierSpec.Bump.PATCH), "1.2.4.0"),
        arrayOf("1.2.3.4", GitVersionModifierSpec(bump = GitVersionModifierSpec.Bump.BUILD), "1.2.3.5"),
        arrayOf("10.2.3", GitVersionModifierSpec(bump = GitVersionModifierSpec.Bump.MAJOR), "11.0.0"),
        arrayOf("1.20.3", GitVersionModifierSpec(bump = GitVersionModifierSpec.Bump.MINOR), "1.21.0"),
        arrayOf("1.2.300", GitVersionModifierSpec(bump = GitVersionModifierSpec.Bump.PATCH), "1.2.301"),
        arrayOf("1.2.3", GitVersionModifierSpec(labels = setOf("beta")), "1.2.3-beta"),
        arrayOf("1.2.3-beta", GitVersionModifierSpec(labels = emptySet()), "1.2.3"),
        arrayOf("1.2.3-beta", GitVersionModifierSpec(labels = setOf("rc.1")), "1.2.3-rc.1"),
        arrayOf("1.2.3", GitVersionModifierSpec(metadata = setOf("001")), "1.2.3+001"),
        arrayOf("1.2.3+001", GitVersionModifierSpec(metadata = emptySet()), "1.2.3"),
        arrayOf("1.2.3+001", GitVersionModifierSpec(metadata = setOf("exp.sha.5114f85")), "1.2.3+exp.sha.5114f85"),
        arrayOf("1.2.3-beta+001", GitVersionModifierSpec(labels = emptySet()), "1.2.3+001"),
        arrayOf("1.2.3-beta+001", GitVersionModifierSpec(metadata = emptySet()), "1.2.3-beta"),
        arrayOf(
            "1.2.3-beta+001", GitVersionModifierSpec(
                bump = GitVersionModifierSpec.Bump.MINOR,
                labels = setOf("beta", "rc.1"),
                metadata = setOf("002", "exp.sha.5114f85")
            ), "1.3.0-beta-rc.1+002+exp.sha.5114f85"
        ),
        arrayOf("1.3.0-rc.1+002+exp.sha.5114f85", GitVersionModifierSpec(), "1.3.0-rc.1+002+exp.sha.5114f85"),
        arrayOf("invalid-version", GitVersionModifierSpec(bump = GitVersionModifierSpec.Bump.MAJOR), "invalid-version"),
        arrayOf(
            "1.2.3",
            GitVersionModifierSpec(bump = GitVersionModifierSpec.Bump.MINOR, labels = setOf("SNAPSHOT")),
            "1.3.0-SNAPSHOT"
        ),
    )

}
