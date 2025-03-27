package io.github.gmazzo.gitversion

open class GitVersionRegexBasedProducer(
    private val rules: List<Pair<Regex, String>>,
) : GitVersionProducer {

    constructor(vararg rules: Pair<Regex, String>) : this(rules.toList())

    override fun versionFor(gitDescribe: String): String {
        for ((regex, replacement) in rules) {
            val replaced = gitDescribe.replace(regex, replacement)
            if (replaced != gitDescribe) {
                return replaced
            }
        }
        return gitDescribe
    }

    data object Snapshot : GitVersionRegexBasedProducer(
        "^\\w{7}$".toRegex() to "0.1.0-SNAPSHOT",
        "-\\d+-\\w{7}$".toRegex() to "-SNAPSHOT",
    ) {
        @Suppress("unused")
        private fun readResolve(): Any = Snapshot
    }
}
