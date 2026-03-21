allprojects {
    gradle.projectsEvaluated {
        tasks.withType<JavaCompile> {
            tasks.withType(JavaCompile::class) {
                options.compilerArgs.addAll(arrayOf("-Xmaxerrs", "100000"))
            }
        }
    }

    repositories {
        mavenLocal()

        maven("https://prmaven.neoforged.net/NeoForge/pr2879") {
            name = "NeoForge 26.1 Snapshot Builds" // https://github.com/neoforged/NeoForge/pull/2879
            content {
                includeModule("net.neoforged", "neoforge")
                includeModule("net.neoforged", "testframework")
            }
        }
    }
}