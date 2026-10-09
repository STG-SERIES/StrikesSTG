tasks.register("buildAll") {
    dependsOn(
        ":26.1:build",
        ":26.1.2:build",
        ":26.2:build",
        ":26.3:build"
    )
}
