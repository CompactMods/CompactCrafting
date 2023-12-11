import java.text.SimpleDateFormat
import java.util.*

plugins {
    id("java-library")
    id("eclipse")
    id("idea")
    id("maven-publish")
    id("net.neoforged.gradle.userdev") version ("7.0.57")
}

var envVersion: String = System.getenv("CC_VERSION") ?: "9.9.9"
if (envVersion.startsWith("v"))
    envVersion = envVersion.trimStart('v');

val mod_id: String by extra
val isRelease: Boolean = (System.getenv("CC_RELEASE") ?: "false").equals("true", true)

val neoforge_version: String by extra
val coreVersion: String = property("core_version") as String

base {
    archivesName.set(mod_id)
    group = "dev.compactmods"
    version = envVersion
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

jarJar.enable()

sourceSets.named("main") {
    java.srcDir("src/main/java")
    resources {
        srcDir("src/main/resources")
        srcDir("src/generated/resources")
    }
}

sourceSets.named("test") {
    java.srcDir("src/test/java")
    resources {
        srcDir("src/test/resources")
    }
}

minecraft.accessTransformers.file(project.file("src/main/resources/META-INF/accesstransformer.cfg"))

runs {
    // applies to all the run configs below
    configureEach {
        // Recommended logging data for a userdev environment
        systemProperty("forge.logging.markers", "") // 'SCAN,REGISTRIES,REGISTRYDUMP'

        // Recommended logging level for the console
        systemProperty("forge.logging.console.level", "debug")

        dependencies {
//            runtime("dev.compactmods.compactcrafting:core-api:$coreVersion")
            runtime("io.reactivex.rxjava3:rxjava:3.1.5")
        }

        // ideaModule("Compact_Crafting.forge-main.main")
        modSource(project.sourceSets.main.get())
    }

    create("client") {
        // Comma-separated list of namespaces to load gametests from. Empty = all namespaces.
        systemProperty("forge.enabledGameTestNamespaces", mod_id)

        programArguments("--username", "Nano")
        programArguments("--width", "1920")
        programArguments("--height", "1080")
    }

    create("server") {
        workingDirectory(file("run/server"))
        environmentVariables("CC_TEST_RESOURCES", project.file("src/test/resources").path)
    }

    create("data") {
        workingDirectory(file("run/data"))

        programArguments("--mod", "compactcrafting")
        programArguments("--all")
        programArguments("--output", file("src/generated/resources/").path)
        programArguments("--existing", file("src/main/resources").path)
    }

    create("gameTestServer") {
        workingDirectory(file("run/gametest"))
        environmentVariables("CC_TEST_RESOURCES", file("src/test/resources").path)
    }
}

repositories {
    mavenLocal()

    maven("https://maven.pkg.github.com/compactmods/compactcrafting-core") {
        name = "Github PKG Core"
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.token") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation("net.neoforged:neoforge:${neoforge_version}")

    implementation("dev.compactmods.compactcrafting", "core-api", coreVersion)
    jarJar("dev.compactmods.compactcrafting", "core-api", "[$coreVersion]") {
        isTransitive = false
    }

    implementation("io.reactivex.rxjava3", "rxjava", "3.1.5")
    jarJar("io.reactivex.rxjava3", "rxjava", "[3.1.0,3.2)")
    jarJar("org.reactivestreams", "reactive-streams", "[1.0.4,)")
}

//
//repositories {
//    mavenLocal()
//
//    maven("https://www.cursemaven.com") {
//        content {
//            includeGroup("curse.maven")
//        }
//    }
//
//    // location of the maven that hosts JEI files
//    maven("https://dvs1.progwml6.com/files/maven") {
//        name = "Progwml Repo"
//    }
//}
//
//val jei_version: String? by extra
//val jei_mc_version: String by extra
//dependencies {
//    // Specify the version of Minecraft to use, If this is any group other then 'net.minecraft' it is assumed
//    // that the dep is a ForgeGradle 'patcher' dependency. And it's patches will be applied.
//    // The userdev artifact is a special name and will get all sorts of transformations applied to it.
//    minecraft("net.minecraftforge", "forge", "${minecraft_version}-${forge_version}")
//
//    implementation(project(":forge-api"))
//    testImplementation(project(":forge-api"))
//

//
//    // Nicephore - Screenshots and Stuff
//    // runtimeOnly(fg.deobf("curse.maven:nicephore-401014:3823401"))
//
//    // JEI
//    compileOnly(fg.deobf("mezz.jei:jei-${jei_mc_version}-common-api:${jei_version}"))
//    compileOnly(fg.deobf("mezz.jei:jei-${jei_mc_version}-forge-api:${jei_version}"))
//    runtimeOnly(fg.deobf("mezz.jei:jei-${jei_mc_version}-forge:${jei_version}"))
//
//    // The One Probe
//    implementation(fg.deobf("curse.maven:theoneprobe-245211:3871444"))
//
//    // Spark
//    runtimeOnly(fg.deobf("curse.maven:spark-361579:3875647"))
//}
//

tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8";
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
//
tasks.jar {
    archiveClassifier.set("slim")
    finalizedBy("reobfJar")
}

tasks.jarJar {
    archiveClassifier.set("")
    finalizedBy("reobfJarJar")
}

val PACKAGES_URL = System.getenv("GH_PKG_URL") ?: "https://maven.pkg.github.com/compactmods/compactcrafting"
publishing {
    publications.register<MavenPublication>("main") {
        artifactId = mod_id
        groupId = "dev.compactmods"

        artifacts {
            artifact(tasks.jar.get())
            artifact(tasks.jarJar.get())
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