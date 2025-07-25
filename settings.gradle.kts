//pluginManagement {
//    repositories {
//        google() // ✅ Don't filter groups here
//        mavenCentral()
//        gradlePluginPortal()
//    }
//}
//
//dependencyResolutionManagement {
//    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
//    repositories {
//        google()
//        mavenCentral()
//    }
//}
//
//rootProject.name = "PlantCareLite"
//include(":app")

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }

    plugins {
        id("com.android.application") version "8.9.2" apply false
        id("org.jetbrains.kotlin.android") version "1.9.23" apply false
        id("com.google.gms.google-services") version "4.4.0" apply false
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "PlantCareLite"
include(":app")
