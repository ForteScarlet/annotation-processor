plugins {
    java
}

group = "love.forte.annotationTool"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}


subprojects {
    apply(plugin = "java")

    repositories {
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
}


