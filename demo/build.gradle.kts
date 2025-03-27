plugins {
    java
    `maven-publish`
    id("io.github.gmazzo.gitversion")
}

group = "org.test"

java {
    withSourcesJar()
    withJavadocJar()
}

publishing.publications.create<MavenPublication>("maven") {
    from(components["java"])
}
