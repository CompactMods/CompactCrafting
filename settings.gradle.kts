pluginManagement {
    plugins {
        id("idea")
        id("eclipse")
        id("maven-publish")
    }

    repositories {
        mavenCentral()
        mavenLocal()

        maven("https://maven.parchmentmc.org") {
            name = "ParchmentMC"
        }

        maven("https://maven.minecraftforge.net") {
            name = "Forge"
        }
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "net.minecraftforge.gradle") {
                useModule("${requested.id}:ForgeGradle:${requested.version}")
            }
        }
    }
}

rootProject.name = "Compact Crafting"
include("forge-api", "forge-main")