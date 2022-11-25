import java.text.SimpleDateFormat
import java.util.*

plugins {
    id("idea")
    id("eclipse")
    id("maven-publish")
    id("net.minecraftforge.gradle") version ("5.1.+")
    id("org.parchmentmc.librarian.forgegradle") version ("1.+")
}

val mod_id: String by extra
val semver: String = System.getenv("CC_SEMVER_VERSION") ?: "9.9.9"
val buildNumber: String = System.getenv("CC_BUILD_NUM") ?: "0"
val nightlyVersion: String = "${semver}.${buildNumber}-nightly"
val isRelease: Boolean = (System.getenv("CC_RELEASE") ?: "false").equals("true", true)
val modVersion = if (isRelease) semver else nightlyVersion

val minecraft_version: String by extra
val forge_version: String by extra
val parchment_version: String by extra

tasks.create("getBuildInfo") {
    doFirst {
        this.logger.info("Mod ID: ${mod_id}")
        this.logger.info("Version: ${modVersion}")
        this.logger.info("Semver Version: ${semver}")
        this.logger.info("Nightly Build: ${nightlyVersion}")
    }
}

base {
    archivesName.set(mod_id)
    group = "dev.compactmods"
    version = if (isRelease) semver else nightlyVersion
}

println("Mod ID: ${mod_id}");
println("Version: ${version}");

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

jarJar.enable()

sourceSets {
    create("api") {
        java.srcDir("src/api/java")
        compileClasspath += configurations.getByName("minecraft")
    }

    named("main") {
        java.srcDir("src/main/java")
        resources {
            srcDir("src/main/resources")
            srcDir("src/generated/resources")
        }

        runtimeClasspath += sourceSets.named("api").get().output
        compileClasspath += sourceSets.named("api").get().output
    }

    named("test") {
        java.srcDir("src/test/java")
        resources {
            srcDir("src/test/resources")
        }

        runtimeClasspath += sourceSets.named("api").get().output
        compileClasspath += sourceSets.named("api").get().output
    }
}

minecraft {
    mappings("parchment", parchment_version)

    // makeObfSourceJar = false // an Srg named sources jar is made by default. uncomment this to disable.
    accessTransformer(file("src/main/resources/META-INF/accesstransformer.cfg"))

    // Default run configurations.
    // These can be tweaked, removed, or duplicated as needed.
    runs {
        all {
            property("mixin.env.remapRefMap", "true")
            property("mixin.env.refMapRemappingFile", "${buildDir}/createSrgToMcp/output.srg")

            // Recommended logging data for a userdev environment
            property("forge.logging.markers", "") // 'SCAN,REGISTRIES,REGISTRYDUMP'

            // Recommended logging level for the console
            property("forge.logging.console.level", "debug")

            mods.create(mod_id) {
                source(sourceSets.named("api").get())
                source(sourceSets.main.get())
            }
        }

        create("client") {
            workingDirectory("run/client")
        }

        create("server") {
            workingDirectory("run/server")
            environment("CC_TEST_RESOURCES", file("src/test/resources"))

            mods.named(mod_id) {
                source(sourceSets.test.get())
            }
        }

        create("data") {
            workingDirectory("run/data")

            args("--mod", "compactcrafting")
            args("--all")
            args("--output", file("src/generated/resources/"))
            args("--existing", file("src/main/resources"))

            forceExit(false)
        }

        create("gameTestServer") {
            workingDirectory("run/gametest")
            environment("CC_TEST_RESOURCES", file("src/test/resources"))

            forceExit(false)

            mods.named(mod_id) {
                source(sourceSets.test.get())
            }
        }
    }
}

repositories {
    mavenLocal()

    maven("https://www.cursemaven.com") {
        content {
            includeGroup("curse.maven")
        }
    }

    // location of the maven that hosts JEI files
    maven("https://dvs1.progwml6.com/files/maven") {
        name = "Progwml Repo"
    }

    // TheOneProbe
    maven("https://maven.tterrag.com/") {
        name = "tterrag maven"
    }
}

val jei_version: String? by extra
val jei_mc_version: String by extra
dependencies {
    // Specify the version of Minecraft to use, If this is any group other then 'net.minecraft' it is assumed
    // that the dep is a ForgeGradle 'patcher' dependency. And it's patches will be applied.
    // The userdev artifact is a special name and will get all sorts of transformations applied to it.
    minecraft("net.minecraftforge", "forge", "${minecraft_version}-${forge_version}")

    minecraftLibrary("io.reactivex.rxjava3", "rxjava", "3.1.5")
    jarJar("io.reactivex.rxjava3", "rxjava", "3.1.5")

    // Nicephore - Screenshots and Stuff
    // runtimeOnly(fg.deobf("curse.maven:nicephore-401014:3823401"))

    // JEI
    compileOnly(fg.deobf("mezz.jei:jei-${jei_mc_version}-common-api:${jei_version}"))
    compileOnly(fg.deobf("mezz.jei:jei-${jei_mc_version}-forge-api:${jei_version}"))
    runtimeOnly(fg.deobf("mezz.jei:jei-${jei_mc_version}-forge:${jei_version}"))

    // The One Probe
    implementation(fg.deobf("curse.maven:theoneprobe-245211:3871444"))

    // Spark
    runtimeOnly(fg.deobf("curse.maven:spark-361579:3875647"))
}

tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}


reobf {
    jarJar {}
}



tasks.withType<Jar> {
    manifest {
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date())
        attributes(mapOf(
                "Specification-Title" to "Compact Crafting",
                "Specification-Vendor" to "",
                "Specification-Version" to "1",
                "Implementation-Title" to "Compact Crafting",
                "Implementation-Version" to archiveVersion,
                "Implementation-Vendor" to "",
                "Implementation-Timestamp" to now
        ))
    }
}

val srcApi = sourceSets.named("api")
val srcMain = sourceSets.named("main")

tasks.create<Jar>("apiJar") {
    archiveClassifier.set("api")

    // Sources included because of MinecraftForge/ForgeGradle#369
    from(srcApi.get().output)
}

tasks.jar {
    archiveClassifier.set("slim")

    from(srcApi.get().output)
    from(srcMain.get().output)

    finalizedBy("reobfJar")
}

tasks.jarJar {
    archiveClassifier.set("")

    from(srcApi.get().output)
    from(srcMain.get().output)
    exclude("LICENSE*")

    finalizedBy("reobfJarJar")
}

artifacts {
    archives(tasks.jar.get())
    archives(tasks.named("apiJar").get())
}

publishing {
    publications.register<MavenPublication>("main") {
        artifactId = mod_id
        groupId = "dev.compactmods"

        artifacts {
            artifact(tasks.named("apiJar").get())
            artifact(tasks.jar.get())
            artifact(tasks.jarJar.get())
        }
    }

    repositories {
        // GitHub Packages
        maven("https://maven.pkg.github.com/CompactMods/CompactCrafting") {
            name = "GitHubPackages"
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}