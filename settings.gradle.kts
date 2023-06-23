pluginManagement {
    plugins {
        id("idea")
        id("eclipse")
        id("maven-publish")

        id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
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
include("forge-main")