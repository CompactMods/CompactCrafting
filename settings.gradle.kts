dependencyResolutionManagement {
    addVersionCatalog(this, "neoforged")
    addVersionCatalog(this, "mojang")
    addVersionCatalog(this, "compactmods")
    addVersionCatalog(this, "mods")
}

pluginManagement {
    plugins {
        id("idea")
        id("eclipse")
        id("maven-publish")
    }

    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()

        // maven("https://maven.architectury.dev/")

        maven("https://maven.parchmentmc.org") {
            name = "ParchmentMC"
        }

        maven("https://maven.neoforged.net/releases") {
            name = "NeoForged"
        }

        maven("https://prmaven.neoforged.net/NeoForge/pr2879") {
            name = "NeoForge 26.1 Snapshot Builds" // https://github.com/neoforged/NeoForge/pull/2879
            content {
                includeModule("net.neoforged", "neoforge")
                includeModule("net.neoforged", "testframework")
            }
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version ("1.0.0")
}

rootProject.name = "Compact Crafting"

include(":neoforge-api")
include(":neoforge-main")
include(":neoforge-datagen")

fun addVersionCatalog(dependencyResolutionManagement: DependencyResolutionManagement, name: String) {
    dependencyResolutionManagement.versionCatalogs.create(name) {
        from(files("./gradle/$name.versions.toml"))
    }
}