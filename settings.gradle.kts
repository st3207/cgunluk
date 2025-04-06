// settings.gradle.kts

pluginManagement {
    repositories {
        google() // Google'ın Maven deposu
        mavenCentral() // Merkezi Maven deposu
        gradlePluginPortal() // Gradle Plugin Portalı
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "cgunluk"
include(":app")