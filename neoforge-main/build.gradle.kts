@file:Suppress("SpellCheckingInspection")

import java.text.SimpleDateFormat
import java.util.*

var envVersion: String = System.getenv("VERSION") ?: "9.9.9"
if (envVersion.startsWith("v"))
    envVersion = envVersion.trimStart('v')

val modId: String = rootProject.property("mod_id") as String
val isRelease: Boolean = (System.getenv("RELEASE") ?: "false").equals("true", true)

val coreApi = project(":neoforge-api")

plugins {
    java
    id("idea")
    id("eclipse")
    id("maven-publish")
    alias(neoforged.plugins.moddev)
}

project.evaluationDependsOn(coreApi.path)

base {
    archivesName.set(modId)
    group = "dev.compactmods.compactcrafting"
    version = envVersion
}

java {
//    toolchain.vendor.set(JvmVendorSpec.JETBRAINS)
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

sourceSets.main {
    resources.srcDir("src/generated/resources")
}

neoForge {
    version = neoforged.versions.neoforge.get()

    this.mods.create(modId) {
        modSourceSets.add(coreApi.sourceSets.main)
        modSourceSets.add(sourceSets.main)
    }

    unitTest {
        enable()
        testedMod = mods.named(modId)
    }
}

neoForge.runs {
    configureEach {
        logLevel.set(org.slf4j.event.Level.DEBUG)
        sourceSet = project.sourceSets.main

        // JetBrains Runtime Hotswap
        if (!System.getenv().containsKey("CI")) {
            jvmArgument("-XX:+AllowEnhancedClassRedefinition")
        }
    }

    register("client") {
        client()
        gameDirectory.set(file("runs/client"))

        programArguments.addAll("--username", "Nano")
        programArguments.addAll("--width", "1920")
        programArguments.addAll("--height", "1080")
    }

    register("client2") {
        client()
        gameDirectory.set(file("runs/client"))

        programArguments.addAll("--username", "Nano2")
        programArguments.addAll("--width", "1920")
        programArguments.addAll("--height", "1080")
    }

    register("server") {
        server()
        gameDirectory.set(file("runs/server"))

        programArgument("nogui")

        systemProperty("neoforge.enabledGameTestNamespaces", modId)
        environment.put("CC_TEST_RESOURCES", file("src/test/resources").path)

        sourceSet = sourceSets.test
    }

    register("GameTests") {
        this.type.set("gameTestServer")
        gameDirectory.set(file("runs/gametest"))

        systemProperty("neoforge.enabledGameTestNamespaces", modId)
        environment.put("CC_TEST_RESOURCES", file("src/test/resources").path)

        sourceSet = sourceSets.test
    }
}

repositories {
    mavenLocal()
    mavenCentral()

    maven("https://maven.blamejared.com/") {
        // location of the maven that hosts JEI files since January 2023
        name = "Jared's maven"
    }

    maven("https://www.cursemaven.com") {
        content {
            includeGroup("curse.maven")
        }
    }

    maven("https://modmaven.dev") {
        // location of a maven mirror for JEI files, as a fallback
        name = "ModMaven"
    }

    maven("https://maven.pkg.github.com/compactmods/spatial") {
        name = "Github PKG Core"
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.token") as String? ?: System.getenv("GITHUB_TOKEN")
        }

        content {
            val m = compactmods.spatial.get().module
            includeModule(m.group, m.name)
        }
    }

    maven("https://maven.pkg.github.com/compactmods/gander") {
        name = "Github PKG Core"
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.token") as String? ?: System.getenv("GITHUB_TOKEN")
        }

        content {
            includeGroup("dev.compactmods.gander")
        }
    }
}

dependencies {
    compileOnly(coreApi)
    testCompileOnly(coreApi)
    jarJar(coreApi)

    implementation(libs.rxjava)
    jarJar(libs.rxjava)
    jarJar(libs.reactivestreams)

    testImplementation(neoforged.testframework)
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    jarJar(compactmods.bundles.gander)
    implementation(compactmods.bundles.gander)
    accessTransformers(compactmods.ganderRendering)

    // ADDITIONAL COMPAT
    compileOnly(mods.bundles.jei)
}

tasks.withType<Test> {
    useJUnitPlatform()
    environment.put("CC_TEST_RESOURCES", file("src/test/resources").path)
}

tasks.withType<ProcessResources>().configureEach {
    duplicatesStrategy = DuplicatesStrategy.WARN

    val replaceProperties: Map<String, Any> = mapOf(
        "minecraft_version" to mojang.versions.minecraft.get(),
        "neo_version" to neoforged.versions.neoforge.get(),
        "minecraft_version_range" to mojang.versions.minecraftRange.get(),
        "neo_version_range" to neoforged.versions.neoforgeRange.get(),
        "loader_version_range" to "[1,)",
        "mod_id" to modId,
        "mod_version" to version
    )

    inputs.properties(replaceProperties)
    filesMatching("META-INF/neoforge.mods.toml") {
        expand(replaceProperties)
    }
}

tasks.withType<Jar> {
    manifest {
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date())
        attributes(
            mapOf(
                "Specification-Title" to "Compact Crafting",
                "Specification-Vendor" to "",
                "Specification-Version" to "1",
                "Implementation-Title" to "Compact Crafting",
                "Implementation-Version" to archiveVersion,
                "Implementation-Vendor" to "",
                "Implementation-Timestamp" to now
            )
        )
    }
}

val PACKAGES_URL = System.getenv("GH_PKG_URL") ?: "https://maven.pkg.github.com/compactmods/compactcrafting"
publishing {
    publications.register<MavenPublication>("main") {
        artifactId = modId
        groupId = "dev.compactmods"
        from(components.getByName("java"))
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