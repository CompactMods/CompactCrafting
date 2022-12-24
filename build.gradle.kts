val mod_id: String by extra
val semver: String = System.getenv("CC_SEMVER_VERSION") ?: "9.9.9"
val buildNumber: String = System.getenv("CC_BUILD_NUM") ?: "0"
val nightlyVersion: String = "${semver}.${buildNumber}-nightly"
val isRelease: Boolean = (System.getenv("CC_RELEASE") ?: "false").equals("true", true)
val modVersion = if (isRelease) semver else nightlyVersion

plugins {
    id("maven-publish")
}

tasks.create("getBuildInfo") {
    doFirst {
        this.logger.info("Mod ID: ${mod_id}")
        this.logger.info("Version: ${modVersion}")
        this.logger.info("Semver Version: ${semver}")
        this.logger.info("Nightly Build: ${nightlyVersion}")
    }
}

val deps = listOf(
        project(":forge-api"),
        project(":forge-main")
)

deps.forEach {
    project.evaluationDependsOn(it.path)
}

val PACKAGES_URL = System.getenv("GH_PKG_URL") ?: "https://maven.pkg.github.com/compactmods/compactcrafting"
publishing {
    publications.register<MavenPublication>("allLibs") {
        artifactId = mod_id
        groupId = "dev.compactmods"
        version = modVersion

        this.artifact(project(":forge-api").tasks.named("jar").get())
        this.artifact(project(":forge-api").tasks.named("sourcesJar").get())
        this.artifact(project(":forge-main").tasks.named("jar").get())
        this.artifact(project(":forge-main").tasks.named("jarJar").get())
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