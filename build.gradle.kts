allprojects {
    gradle.projectsEvaluated {
        tasks.withType<JavaCompile> {
            tasks.withType(JavaCompile::class) {
                options.compilerArgs.addAll(arrayOf("-Xmaxerrs", "100000"))
            }
        }
    }
}