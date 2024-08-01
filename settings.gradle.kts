pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
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
    }
}

rootProject.name = "Wash"
include(":app")
 