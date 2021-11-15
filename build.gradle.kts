plugins {
    `java-library`
    `maven-publish`
    signing
    // see https://github.com/gradle-nexus/publish-plugin
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"

    // id("io.codearte.nexus-staging") version "0.22.0" // apply false
    // io.github.gradle-nexus.publish-plugin
}

buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

group = P.GROUP
version = P.VERSION
description = P.DESCRIPTION

    repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
}

val credentialsUsernameKey = "sonatype.username"
val credentialsPasswordKey = "sonatype.password"
val secretKeyRingFileKey = "signing.secretKeyRingFile"

// set gpg file path to root
val secretKeyRingFile = extra.properties[secretKeyRingFileKey]!!.toString()
val secretRingFile = File(project.rootDir, secretKeyRingFile)
extra[secretKeyRingFileKey] = secretRingFile


subprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")

    repositories {
        mavenLocal()
        mavenCentral()
    }

    group = P.GROUP
    version = P.VERSION
    description = P.DESCRIPTION + " - $name module"

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
    println("[publishing-configure] - [$name] configured.")
    setProperty(secretKeyRingFileKey, secretRingFile)

    signing {
        sign(publishing.publications)
    }
}




// nexus staging

val credentialsUsername: String = extra.properties[credentialsUsernameKey]!!.toString()
val credentialsPassword: String = extra.properties[credentialsPasswordKey]!!.toString()


nexusPublishing {
    packageGroup.set(P.GROUP)

    repositories {
        sonatype {
            // stagingProfileId.set(credentialsUsername)
            // nexusUrl.set(uri(Sonatype.oss.URL))
            // snapshotRepositoryUrl.set(uri(Sonatype.`snapshot-oss`.URL))
            username.set(credentialsUsername)
            password.set(credentialsPassword)
        }

    }
}


