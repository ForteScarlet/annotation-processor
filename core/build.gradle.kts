plugins {
    java
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