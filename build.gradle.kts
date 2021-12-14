/*
 *  Copyright (c) 2021-2021 ForteScarlet <https://github.com/ForteScarlet>
 *
 *  根据 Apache License 2.0 获得许可；
 *  除非遵守许可，否则您不得使用此文件。
 *  您可以在以下网址获取许可证副本：
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *   有关许可证下的权限和限制的具体语言，请参见许可证。
 */

plugins {
    // `java-library`
    `maven-publish`
    signing
    // see https://github.com/gradle-nexus/publish-plugin
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
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
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")

    repositories {
        mavenLocal()
        mavenCentral()
    }

    group = P.GROUP
    version = P.VERSION
    description = P.DESCRIPTION + " - $name module"


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
            username.set(credentialsUsername)
            password.set(credentialsPassword)
        }

    }
}


