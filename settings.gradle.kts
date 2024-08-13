pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // kakao login
        maven {
            url = uri("https://devrepo.kakao.com/nexus/content/groups/public/")
        }
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "Wash"
include(":app")