//var envVersion: String = System.getenv("CC_VERSION") ?: "9.9.9"
//if(envVersion.startsWith("v"))
//    envVersion = envVersion.trimStart('v');
//
//val mod_id: String by extra
//val isRelease: Boolean = (System.getenv("CC_RELEASE") ?: "false").equals("true", true)
//
//plugins {
//    id("maven-publish")
//}
//
//val deps = listOf(
//        project(":forge-api"),
//        project(":forge-main")
//)
//
//deps.forEach {
//    project.evaluationDependsOn(it.path)
//}
//
//val PACKAGES_URL = System.getenv("GH_PKG_URL") ?: "https://maven.pkg.github.com/compactmods/compactcrafting"
//publishing {
//    publications.register<MavenPublication>("allLibs") {
//        artifactId = mod_id
//        groupId = "dev.compactmods"
//        version = envVersion
//
//        this.artifact(project(":forge-api").tasks.named("jar").get())
//        this.artifact(project(":forge-api").tasks.named("sourcesJar").get())
//        this.artifact(project(":forge-main").tasks.named("jar").get())
//        this.artifact(project(":forge-main").tasks.named("jarJar").get())
//    }
//
//    repositories {
//        // GitHub Packages
//        maven(PACKAGES_URL) {
//            name = "GitHubPackages"
//            credentials {
//                username = System.getenv("GITHUB_ACTOR")
//                password = System.getenv("GITHUB_TOKEN")
//            }
//        }
//    }
//}