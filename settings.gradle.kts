pluginManagement {
    resolutionStrategy {
        eachPlugin {
            val regex = "com.android.(library|application)".toRegex()
            if (regex matches requested.id.id) {
                useModule("com.android.tools.build:gradle:${requested.version}")
            }
            if (requested.id.id == "dagger.hilt.android.plugin") {
                useModule("com.google.dagger:hilt-android-gradle-plugin:${requested.version}")
            }
        }
    }
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven(url = "https://www.jitpack.io")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
        maven(url = "https://www.jitpack.io")
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "BreezyWeather"
include(":app")
include(":data")
include(":domain")
include(":maps-utils")
include(":ui-weather-view")
include(":weather-unit")

// Motion Core from INFORMATION89/Motion-UI (Apache-2.0) is a git submodule at motion/, built from source: its
// `io.github.information89:motion` is that build's `:motion` project. Clone Solstice with
// `--recurse-submodules` (or run `git submodule update --init`). Workflow: docs/CONTINUE.md, "Motion".
includeBuild("motion") {
    dependencySubstitution {
        substitute(module("io.github.information89:motion")).using(project(":motion"))
    }
}

// To test local changes
/*includeBuild("../breezy-weather-data-sharing-lib/") {
    dependencySubstitution {
        substitute(module("com.github.breezy-weather:breezy-weather-data-sharing-lib"))
            .using(project(":lib"))
    }
}*/
