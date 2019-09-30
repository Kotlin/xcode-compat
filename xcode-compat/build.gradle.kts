plugins {
    `kotlin-dsl`
    `maven-publish`
    id("com.gradle.plugin-publish") version "0.10.1"
}

group = "org.jetbrains.kotlin.xcode-compat"
version = "0.2.4"

val PLUGIN_NAME = "xcode-compat"

gradlePlugin {
    plugins {
        register(PLUGIN_NAME) {
            id = "org.jetbrains.kotlin.xcode-compat"
            implementationClass = "org.jetbrains.kotlin.xcodecompat.XcodeCompatPlugin"
        }
    }
}

dependencies {
    implementation("org.jetbrains.kotlin", "kotlin-gradle-plugin", "1.3.20")
}

publishing {
    repositories {
        
    }
}

repositories {
    jcenter()
}


pluginBundle {
    website = "https://kotlinlang.org/"
    vcsUrl = "https://github.com/JetBrains/kotlin/"

    (plugins) {
        PLUGIN_NAME {
            displayName = "Xcode compatibility for Kotlin MPP"
            description = "This plugin simplifies integration of Kotlin MPP into Xcode projects"
            tags = listOf("Kotlin")
            version = rootProject.version.toString()
        }
    }
}