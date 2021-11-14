
import org.gradle.api.Project
import org.gradle.api.*
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.*



inline fun Project.configurePublishing(
    artifactId: String,
    vcs: String = "https://github.com/ForteScarlet/annotation-tool",
) {
    // configureRemoteRepos()
    // apply<ShadowPlugin>()


    val sourcesJar by tasks.registering(Jar::class) {
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
    }
    // val sourcesJar = tasks["sourcesJar"]
    val javadocJar = tasks.register("javadocJar", Jar::class) {
        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        archiveClassifier.set("javadoc")
    }

    publishing {
        publications {
            register("mavenJava", MavenPublication::class) {
                from(components["java"])

                groupId = rootProject.group.toString()
                setArtifactId(artifactId)
                version = project.version.toString()

                setupPom(
                    project = project,
                    vcs = vcs
                )

                artifact(sourcesJar)
                artifact(javadocJar.get())
            }
        }

        repositories {
            maven {
                if (version.toString().endsWith("SNAPSHOTS", true)) {
                    // snapshot
                    name = "snapshots-oss"
                    url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
                } else {
                    name = "oss"
                    url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                }
                credentials {
                    username = project.extra.properties["sonatype.username"]?.toString()
                        ?: throw NullPointerException("snapshots-sonatype-username")
                    password = project.extra.properties["sonatype.password"]?.toString()
                        ?: throw NullPointerException("snapshots-sonatype-password")
                }
            }
        }

        // configGpgSign(this@configurePublishing)
    }
}


fun MavenPublication.setupPom(
    project: Project,
    vcs: String = "https://github.com/ForteScarlet/annotation-tool"
) {
    pom {
        scm {
            url.set(vcs)
            connection.set("scm:$vcs.git")
            developerConnection.set("scm:${vcs.replace("https:", "git:")}.git")
        }

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://github.com/ForteScarlet/kron/blob/master/LICENSE")
            }
        }

        developers {
            developer {
                id.set("forte")
                name.set("ForteScarlet")
                email.set("ForteScarlet@163.com")
            }
        }

    }

    pom.withXml {
        val root = asNode()
        root.appendNode("description", project.description)
        root.appendNode("name", project.name)
        root.appendNode("url", vcs)
    }
}