plugins {
    java
    id("maven-publish")
    id("signing")
    id("io.codearte.nexus-staging") version "0.22.0" apply false

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

