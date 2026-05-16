pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()

    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }  // Add this if you need JitPack dependencies
    }
}

rootProject.name = "Gold Leaf Farmer Portal"
// Core and app modules
include(":app")
include(":core")
include(":feature-farmer-management")
include(":feature-crop-management")
include(":feature-weather-climate")
include(":feature-advisory-services")
include(":feature-training-extension")
include(":feature-certification-quality")