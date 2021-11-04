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
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
        compileOnly("org.jetbrains:annotations:22.0.0")
        testCompileOnly("org.jetbrains:annotations:22.0.0")
    }

    tasks.getByName<Test>("test") {
        useJUnitPlatform()
    }
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}