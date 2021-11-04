plugins {
    `kotlin-dsl`
}

repositories {
    mavenLocal()
    mavenCentral()
}


dependencies {
    api(gradleApi())
    api("org.jetbrains.kotlin", "kotlin-gradle-plugin", "1.5.31")
    api("org.jetbrains.kotlin", "kotlin-compiler-embeddable", "1.5.31")
}

