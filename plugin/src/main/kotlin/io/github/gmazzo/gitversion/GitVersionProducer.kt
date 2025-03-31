package io.github.gmazzo.gitversion

import java.io.Serializable
import org.gradle.api.HasImplicitReceiver

@HasImplicitReceiver
fun interface GitVersionProducer : Serializable {

    fun produceVersion(source: GitVersionValueSource): String

}
