import net.minecraftforge.gradle.userdev.UserDevExtension
import java.text.SimpleDateFormat
import java.util.*

plugins {
    id("idea")
    id("eclipse")
    id("maven-publish")
    id("net.minecraftforge.gradle") version ("5.1.+")
    id("org.parchmentmc.librarian.forgegradle") version ("1.+")
}

var envVersion: String = System.getenv("CC_VERSION") ?: "9.9.9"
if(envVersion.startsWith("v"))
    envVersion = envVersion.trimStart('v');

var mod_id: String by extra
val isRelease: Boolean = (System.getenv("CC_RELEASE") ?: "false").equals("true", true)

var minecraft_version: String by extra
var forge_version: String by extra
var parchment_version: String by extra

base {
    archivesName.set(mod_id)
    group = "dev.compactmods"
    version = envVersion
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    withSourcesJar()
}

sourceSets.named("main") {
    resources {
        //The API has no resources
        setSrcDirs(emptyList<String>())
    }
}

sourceSets.named("test") {
    resources {
        //The test module has no resources
        setSrcDirs(emptyList<String>())
    }
}

configure<UserDevExtension> {
    mappings("parchment", parchment_version)
    accessTransformer(file("../forge-main/src/main/resources/META-INF/accesstransformer.cfg"))
}

dependencies {
    minecraft ("net.minecraftforge", "forge", version = "${minecraft_version}-${forge_version}")
}

tasks.withType<Jar> {
    manifest {
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date())
        attributes(mapOf(
                "Specification-Title" to "Compact Crafting API",
                "Specification-Vendor" to "",
                "Specification-Version" to "1", // We are version 1 of ourselves
                "Implementation-Title" to "Compact Crafting API",
                "Implementation-Version" to archiveVersion,
                "Implementation-Vendor" to "",
                "Implementation-Timestamp" to now
        ))
    }
}

tasks.jar {
    archiveClassifier.set("api")
    finalizedBy("reobfJar")
}

tasks.named<Jar>("sourcesJar") {
    archiveClassifier.set("api-sources")
}

artifacts {
    archives(tasks.jar.get())
    archives(tasks.named("sourcesJar").get())
}

val PACKAGES_URL = System.getenv("GH_PKG_URL") ?: "https://maven.pkg.github.com/compactmods/compactcrafting"
publishing {
    publications.register<MavenPublication>("releaseApi") {
        artifactId = mod_id
        groupId = "dev.compactmods"

        artifacts {
            artifact(tasks.jar.get())
            artifact(tasks.named("sourcesJar").get())
        }
    }

    repositories {
        // GitHub Packages
        maven(PACKAGES_URL) {
            name = "GitHubPackages"
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}