package io.github.gmazzo.gitversion

// honors https://semver.org/ spec
public data class GitVersionModifierSpec(
    val bump: Bump? = null,
    val labels: Set<String>? = null,
    val metadata: Set<String>? = null,
    val storeTag: Boolean = false,
) {

    public fun modify(version: String): String = buildString {
        val match = VERSION_REGEX.matchEntire(version)?.groups ?: return version

        when (bump) {
            Bump.MAJOR -> append(match["major"]!!.value.toInt() + 1).append(".0").run {
                if (match["patch"] != null) append(".0")
                if (match["build"] != null) append(".0")
            }
            Bump.MINOR -> append(match["major"]!!.value).append(".")
                .append(match["minor"]!!.value.toInt() + 1).run {
                    if (match["patch"] != null) append(".0")
                    if (match["build"] != null) append(".0")
                }

            Bump.PATCH -> append(match["major"]!!.value).append(".")
                .append(match["minor"]!!.value).append(".")
                .append((match["patch"]?.value?.toInt() ?: 0) + 1).run {
                    if (match["build"] != null) append(".0")
                }

            Bump.BUILD -> append(match["major"]!!.value).append(".")
                .append(match["minor"]!!.value).append(".")
                .append(match["patch"]?.value ?: "0").append(".")
                .append((match["build"]?.value?.toInt() ?: 0) + 1)

            null -> append(match["version"]!!.value)
        }
        labels?.forEach { append("-").append(it) } ?: append(match["labels"]!!.value)
        metadata?.forEach { append("+").append(it) } ?: append(match["metadata"]!!.value)
    }

    override fun toString(): String = buildString {
        bump?.name?.lowercase()?.let(::append)
        labels?.ifEmpty { listOf("") }?.forEach { if (isNotEmpty()) append(','); append("label=").append(it) }
        metadata?.ifEmpty { listOf("") }?.forEach { if (isNotEmpty()) append(','); append("metadata=").append(it) }
        if (storeTag) {
            if (isNotEmpty()) append(','); append("storeTag")
        }
    }

    public enum class Bump { MAJOR, MINOR, PATCH, BUILD }

    public companion object {

        private val VERSION_REGEX =
            "(?<version>(?<major>\\d+)\\.(?<minor>\\d+)(?:\\.(?<patch>\\d+)(?:\\.(?<build>\\d+))?)?)(?<labels>(?:-[0-9A-Za-z]+)*)(?<metadata>(?:\\+[0-9A-Za-z]+)*)$".toRegex()

        public fun parse(input: String): GitVersionModifierSpec {
            var spec = GitVersionModifierSpec()

            for (part in input.split(',')) {
                val (name, value) = part.split('=', limit = 2).let { it[0] to it.getOrNull(1) }

                when (name) {
                    "major", "minor", "patch", "build" -> {
                        check(value == null) { "Option '$name' should not have a value: $part" }
                        check(spec.bump == null) { "Multiple bump options specified: ${spec.bump} and $part" }
                        spec = spec.copy(bump = Bump.valueOf(part.uppercase()))
                    }

                    "label" -> spec = spec.copy(
                        labels = spec.labels.orEmpty() +
                            listOfNotNull(value?.takeIf(String::isNotBlank))
                    )

                    "metadata" -> spec = spec.copy(
                        metadata = spec.metadata.orEmpty() +
                            listOfNotNull(value?.takeIf(String::isNotBlank))
                    )

                    "storeTag" -> {
                        check(value == null) { "Option '$name' should not have a value: $part" }
                        spec = spec.copy(storeTag = true)
                    }
                }
            }
            return spec
        }

    }

}
