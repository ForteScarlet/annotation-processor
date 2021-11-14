plugins {
    java
    id("maven-publish")
    id("signing")
}

group = P.GROUP // "love.forte.annotationTool"
version = P.VERSION // "0.5.0"

repositories {
    mavenLocal()
    mavenCentral()
}

val credentialsUsernameKey = "sonatype.username"
val credentialsPasswordKey = "sonatype.password"


subprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")

    repositories {
        mavenLocal()
        mavenCentral()
    }


    dependencies {
        testImplementation(V.Jupiter.Api.NOTATION)
        testRuntimeOnly(V.Jupiter.Engine.NOTATION)
        compileOnly(V.Jetbrains.Annotations.NOTATION)
        testCompileOnly(V.Jetbrains.Annotations.NOTATION)
    }

    tasks.getByName<Test>("test") {
        useJUnitPlatform()
    }

    configurePublishing(name)

    signing {
        sign(publishing.publications)
    }
}


// inline fun Project.configurePublishing(
//     artifactId: String,
//     vcs: String = "https://github.com/ForteScarlet/annotation-tool",
// ) {
//     // configureRemoteRepos()
//     // apply<ShadowPlugin>()
//
//
//     val sourcesJar by tasks.registering(Jar::class) {
//         archiveClassifier.set("sources")
//         from(sourceSets["main"].allSource)
//     }
//     // val sourcesJar = tasks["sourcesJar"]
//     val javadocJar = tasks.register("javadocJar", Jar::class) {
//         @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
//         archiveClassifier.set("javadoc")
//     }
//
//     publishing {
//         publications {
//             register("mavenJava", MavenPublication::class) {
//                 from(components["java"])
//
//                 groupId = rootProject.group.toString()
//                 setArtifactId(artifactId)
//                 version = project.version.toString()
//
//                 setupPom(
//                     project = project,
//                     vcs = vcs
//                 )
//
//                 artifact(sourcesJar)
//                 artifact(javadocJar.get())
//             }
//         }
//
//         repositories {
//             maven {
//                 if (version.toString().endsWith("SNAPSHOTS", true)) {
//                     // snapshot
//                     name = "snapshots-oss"
//                     url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
//                 } else {
//                     name = "oss"
//                     url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
//                 }
//                 credentials {
//                     username = project.extra.properties["sonatype.username"]?.toString()
//                         ?: throw NullPointerException("snapshots-sonatype-username")
//                     password = project.extra.properties["sonatype.password"]?.toString()
//                         ?: throw NullPointerException("snapshots-sonatype-password")
//                 }
//             }
//         }
//
//         // configGpgSign(this@configurePublishing)
//     }
// }