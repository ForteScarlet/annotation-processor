plugins {
    java
    id("maven-publish")
    id("signing")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("cn.hutool:hutool-core:5.7.16")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

