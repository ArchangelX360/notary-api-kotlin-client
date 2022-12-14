import java.net.URI

plugins {
    val kotlinVersion = "1.7.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("org.jetbrains.dokka") version "1.7.10"
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    id("maven-publish")
    id("signing")
    id("org.hildan.github.changelog") version "1.11.1"
}

group = "io.github.archangelx360"
description = "Apple Notary API client for Kotlin"

repositories {
    mavenCentral()
}

dependencies {
    val ktorVersion = "2.1.1"
    api("io.ktor:ktor-client-core:$ktorVersion")
    api("io.ktor:ktor-client-logging:$ktorVersion")
    api("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    api("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    implementation("com.amazonaws:aws-java-sdk-s3:1.12.302")

    implementation("com.auth0:java-jwt:4.0.0")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

changelog {
    futureVersionTag = project.version.toString()
}

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val dokkaJavadocJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles Kotlin docs with Dokka into a Javadoc jar"
    archiveClassifier.set("javadoc")
    from(tasks.dokkaJavadoc)
}

nexusPublishing {
    packageGroup.set("io.github.archangelx360")
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            artifact(sourcesJar)
            artifact(dokkaJavadocJar)

            val githubUser: String by project
            val githubSlug = "$githubUser/${rootProject.name}"
            val githubRepoUrl = "https://github.com/$githubSlug"

            pom {
                name.set(project.name)
                description.set(project.description)
                url.set(githubRepoUrl)
                licenses {
                    license {
                        name.set("The MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("ArchangelX360")
                        name.set("Titouan BION")
                        email.set("titouan.bion@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:$githubRepoUrl.git")
                    developerConnection.set("scm:git:git@github.com:$githubSlug.git")
                    url.set(githubRepoUrl)
                }
            }
        }
    }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["maven"])
}
