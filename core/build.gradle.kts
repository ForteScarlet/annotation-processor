plugins {
    java
}

group = "love.forte.annotationTool"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("cn.hutool:hutool-core:5.7.15")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}