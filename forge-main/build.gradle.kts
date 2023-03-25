import java.text.SimpleDateFormat
import java.util.*

val coreVersion: String = rootProject.property("core_version") as String

var semver: String = System.getenv("CC_SEMVER_VERSION") ?: "9.9.9"
if(semver.startsWith("v"))
    semver = semver.trimStart('v');

val buildNumber: String = System.getenv("CC_BUILD_NUM") ?: "0"

val nightlyVersion: String = "${semver}.${buildNumber}-nightly"
val isRelease: Boolean = (System.getenv("CC_RELEASE") ?: "false").equals("true", true)

var mod_id: String by extra
var minecraft_version: String by extra
var forge_version: String by extra
var parchment_version: String by extra

plugins {
    id("idea")
    id("eclipse")
    id("maven-publish")
    id("net.minecraftforge.gradle") version ("5.1.+")
    id("org.parchmentmc.librarian.forgegradle") version ("1.+")
}

base {
    archivesName.set(mod_id)
    group = "dev.compactmods"
    version = if (isRelease) semver else nightlyVersion
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

sourceSets.main {
    java.srcDir("src/main/java")
    resources {
        srcDir("src/main/resources")
        srcDir("src/generated/resources")
    }
}

sourceSets.test {
    java.srcDir("src/test/java")
    resources.srcDir("src/test/resources")
}

jarJar.enable()
repositories {
    mavenLocal()

    mavenCentral() {
        content {
            includeGroup("io.reactivex.rxjava3")
        }
    }

    maven("https://www.cursemaven.com") {
        content {
            includeGroup("curse.maven")
        }
    }

    // location of the maven that hosts JEI files
    maven("https://dvs1.progwml6.com/files/maven") {
        name = "Progwml Repo"
        content {
            includeGroup("mezz.jei")
        }
    }

    maven("https://maven.pkg.github.com/compactmods/compactcrafting-core") {
        name = "Github PKG Core"
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.token") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

val jei_version: String? by extra
val jei_mc_version: String by extra
val top_version = "3965688"
dependencies {
    minecraft("net.minecraftforge", "forge", version = "${minecraft_version}-${forge_version}")

    implementation("dev.compactmods.compactcrafting", "forge-api", coreVersion) {
        isTransitive = false
    }

    jarJar("dev.compactmods.compactcrafting", "forge-api", "[2,3)") {
        isTransitive = false
    }

    minecraftLibrary("io.reactivex.rxjava3", "rxjava", "3.1.5")
    jarJar("io.reactivex.rxjava3", "rxjava", "[3.1.5]")
    jarJar("org.reactivestreams", "reactive-streams", "[1.0.4]")

    // JEI
    if (project.extra.has("jei_version") && project.extra.has("jei_mc_version")) {
        compileOnly(fg.deobf("mezz.jei:jei-${jei_mc_version}-common-api:${jei_version}"))
        compileOnly(fg.deobf("mezz.jei:jei-${jei_mc_version}-forge-api:${jei_version}"))
        runtimeOnly(fg.deobf("mezz.jei:jei-${jei_mc_version}-forge:${jei_version}"))
    }

    // The One Probe
    implementation(fg.deobf("curse.maven:theoneprobe-245211:$top_version"))

//    if(!System.getenv().containsKey("CI")) {
//        // Nicephore - Screenshots and Stuff
//        runtimeOnly(fg.deobf("curse.maven:nicephore-401014:3574658"))
//
//        // Shut up Experimental Settings - so we don't have to deal with that CONSTANTLY
//        runtimeOnly(fg.deobf("curse.maven:shutupexperimental-407174:3544525"))
//
//        runtimeOnly(fg.deobf("mezz.jei:jei-${jei_mc_version}:${jei_version}"))
//    }
}

minecraft {
    mappings("parchment", "${parchment_version}-${minecraft_version}")

    // makeObfSourceJar = false // an Srg named sources jar is made by default. uncomment this to disable.
    accessTransformer(file("src/main/resources/META-INF/accesstransformer.cfg"))

    // Default run configurations.
    // These can be tweaked, removed, or duplicated as needed.
    runs {
        all {
            // Recommended logging data for a userdev environment
            property("forge.logging.markers", "") // "SCAN,REGISTRIES,REGISTRYDUMP"

            // Recommended logging level for the console
            property("forge.logging.console.level", "debug")

            property("mixin.env.remapRefMap", "true")
            property("mixin.env.refMapRemappingFile", "${buildDir}/createSrgToMcp/output.srg")

            ideaModule("Compact_Crafting.forge-main.main")

            mods.create(mod_id) {
                source(sourceSets.main.get())
            }
        }

        create("client") {
            workingDirectory(project.file("run/client"))
            args("--username", "Nano")
            args("--width", 1920)
            args("--height", 1080)
        }

        create("server") {
            taskName("runServer")
            workingDirectory(project.file("run/server"))
            environment("CC_TEST_RESOURCES", file("src/test/resources"))

            mods.named(mod_id) {
                source(sourceSets.test.get())
            }
        }

        create("data") {
            taskName("runData")
            workingDirectory(file("run/data"))
            forceExit(false)

            args("--mod", mod_id)
            args("--existing", project.file("src/main/resources"))
            args("--all")
            args("--output", file("src/generated/resources/"))
        }

        create("gameTestServer") {
            taskName("runGameTestServer")
            workingDirectory(project.file("run/test"))
            environment("CC_TEST_RESOURCES", file("src/test/resources"))
            forceExit(false)

            mods.named(mod_id) {
                source(sourceSets.test.get())
            }
        }
    }
}

reobf {
    this.create("jarJar")
}

tasks.compileJava {
    options.encoding = "UTF-8";
}

tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.jar {
    archiveClassifier.set("slim")
    finalizedBy("reobfJar")
}

tasks.jarJar {
    archiveClassifier.set("")
    finalizedBy("reobfJarJar")
}

artifacts {
    archives(tasks.jar.get())
    archives(tasks.jarJar.get())
}

tasks.withType<Jar> {
    manifest {
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date())
        attributes(mapOf(
                "Specification-Title" to "Compact Crafting",
                "Specification-Vendor" to "",
                "Specification-Version" to "1", // We are version 1 of ourselves
                "Implementation-Title" to "Compact Crafting",
                "Implementation-Version" to archiveVersion,
                "Implementation-Vendor" to "",
                "Implementation-Timestamp" to now
        ))
    }
}

artifacts {
    archives(tasks.jar.get())
    archives(tasks.jarJar.get())
}

val PACKAGES_URL = System.getenv("GH_PKG_URL") ?: "https://maven.pkg.github.com/compactmods/compactcrafting"
publishing {
    publications.register<MavenPublication>("forge") {
        from(components.findByName("java"))
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