plugins {
    `kotlin-dsl`
    java
}

repositories {
    mavenLocal()
    google()
    mavenCentral()
}

kotlin {
    sourceSets.all {
        languageSettings.optIn("kotlin.Experimental")
        languageSettings.optIn("kotlin.RequiresOptIn")
    }
}

dependencies {
    api("org.jetbrains.kotlin", "kotlin-gradle-plugin", "1.5.31")
    api("org.jetbrains.kotlin", "kotlin-compiler-embeddable", "1.5.31")
    api(gradleApi())
}

