pluginManagement {
    repositories {
        mavenCentral()
        mavenLocal()
        maven { url = 'https://maven.minecraftforge.net' }
        maven { url = 'https://maven.parchmentmc.org' }
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