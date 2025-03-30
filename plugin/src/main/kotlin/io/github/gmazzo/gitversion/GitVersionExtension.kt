package io.github.gmazzo.gitversion

import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

interface GitVersionExtension {

    val tagPrefix: Property<String>

    val forceSnapshot : Property<Boolean>

    val initialVersion: Property<String>

    val versionProducer: Property<GitVersionProducer>

    fun versionProducer(producer: GitVersionProducer)

    val version: Provider<String>

}
