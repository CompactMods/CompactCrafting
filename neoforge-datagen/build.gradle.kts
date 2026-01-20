plugins {
    id("java")
    id("eclipse")
    id("idea")
    id("maven-publish")
    alias(neoforged.plugins.moddev)
}

val modId: String = rootProject.property("mod_id") as String

val projectApi = project(":neoforge-api")
val projectMain: Project = project(":neoforge-main")

project.evaluationDependsOn(projectApi.path)
project.evaluationDependsOn(projectMain.path)

java {
//    toolchain.vendor.set(JvmVendorSpec.JETBRAINS)
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

neoForge {
    version = neoforged.versions.neoforge.get()

    mods.register(modId) {
        this.sourceSet(projectApi.sourceSets.main.get())
        this.sourceSet(sourceSets.main.get())
        this.sourceSet(projectMain.sourceSets.main.get())
    }

    runs.register("data") {
        this.clientData()

        this.gameDirectory.set(file("runs/data"))

        programArguments.addAll("--mod", modId)
        programArguments.addAll("--all")
        programArguments.addAll("--output", projectMain.file("src/generated/resources").absolutePath)
        programArguments.addAll("--existing", projectMain.file("src/main/resources").absolutePath)
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    compileOnly(projectApi)
    compileOnly(projectMain)

    implementation(libs.rxjava)
}