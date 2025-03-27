package io.github.gmazzo.gitversion

import java.io.Serializable

fun interface GitVersionProducer : Serializable {

    fun versionFor(gitDescribe: String): String

}
