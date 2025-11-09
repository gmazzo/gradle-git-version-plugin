package io.github.gmazzo.gitversion

import java.io.Serializable
import org.gradle.api.HasImplicitReceiver

@HasImplicitReceiver
public fun interface GitVersionProducer : Serializable {

    public fun produceVersion(source: GitVersionValueSource): String

}
