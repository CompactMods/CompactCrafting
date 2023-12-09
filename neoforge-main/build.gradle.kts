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

base {
    archivesName.set(mod_id)
    group = "dev.compactmods"
    version = envVersion
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

jarJar.enable()

configurations {
    create("mcLibrary") {}
    implementation {
        extendsFrom(configurations.findByName("mcLibrary"))
    }
}

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
            runtime(configuration(configurations.findByName("mcLibrary")))
        }

        // ideaModule("Compact_Crafting.forge-main.main")
        modSource(project.sourceSets.main.get())
    }

    create("client") {
        // Comma-separated list of namespaces to load gametests from. Empty = all namespaces.
        systemProperty("forge.enabledGameTestNamespaces", mod_id)
    }

//    server {
//        systemProperty 'forge.enabledGameTestNamespaces', project.mod_id
//        programArgument '--nogui'
//    }
//
//    // This run config launches GameTestServer and runs all registered gametests, then exits.
//    // By default, the server will crash when no gametests are provided.
//    // The gametest system is also enabled by default for other run configs under the /test command.
//    gameTestServer {
//        systemProperty 'forge.enabledGameTestNamespaces', project.mod_id
//    }
//
//    data {
//        // example of overriding the workingDirectory set in configureEach above, uncomment if you want to use it
//        // workingDirectory project.file('run-data')
//
//        // Specify the modid for data generation, where to output the resulting resource, and where to look for existing resources.
//        programArguments.addAll '--mod', project.mod_id, '--all', '--output', file('src/generated/resources/').getAbsolutePath(), '--existing', file('src/main/resources/').getAbsolutePath()
//    }
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

val coreVersion: String = property("core_version") as String
dependencies {
    implementation("net.neoforged:neoforge:${neoforge_version}")

     implementation("dev.compactmods.compactcrafting", "core-api", coreVersion)
    "mcLibrary" (jarJar("dev.compactmods.compactcrafting", "core-api", "[$coreVersion]") {
        isTransitive = false
    })

    "mcLibrary" (implementation("io.reactivex.rxjava3", "rxjava", "3.1.5"))
    jarJar("io.reactivex.rxjava3", "rxjava", "[3.1.0,3.2)")
    jarJar("org.reactivestreams", "reactive-streams", "[1.0.4,)")
}

//
//minecraft {
//    mappings("parchment", parchment_version)
//
//    // makeObfSourceJar = false // an Srg named sources jar is made by default. uncomment this to disable.
//    accessTransformer(file("src/main/resources/META-INF/accesstransformer.cfg"))
//
//    // Default run configurations.
//    // These can be tweaked, removed, or duplicated as needed.
//    runs {
//        all {
//            property("mixin.env.remapRefMap", "true")
//            property("mixin.env.refMapRemappingFile", "${buildDir}/createSrgToMcp/output.srg")
//
//
//
//            ideaModule("Compact_Crafting.forge-main.main")
//
//            mods.create(mod_id) {
//                source(sourceSets.main.get())
//                for (p in runDepends)
//                    source(p.sourceSets.main.get())
//            }
//        }
//
//        create("client") {
//            taskName("runClient")
//            workingDirectory(file("run/client"))
//
//            args("--username", "Nano")
//            args("--width", 1920)
//            args("--height", 1080)
//        }
//
//        create("server") {
//            taskName("runServer")
//            workingDirectory(file("run/server"))
//            environment("CC_TEST_RESOURCES", file("src/test/resources"))
//
//            mods.named(mod_id) {
//                source(sourceSets.test.get())
//            }
//        }
//
//        create("data") {
//            taskName("runData")
//            workingDirectory(file("run/data"))
//
//            args("--mod", "compactcrafting")
//            args("--all")
//            args("--output", file("src/generated/resources/"))
//            args("--existing", file("src/main/resources"))
//
//            forceExit(false)
//        }
//
//        create("gameTestServer") {
//            taskName("runGameTestServer")
//            workingDirectory(file("run/gametest"))
//            environment("CC_TEST_RESOURCES", file("src/test/resources"))
//
//            forceExit(false)
//
//            mods.named(mod_id) {
//                source(sourceSets.test.get())
//            }
//        }
//    }
//}
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