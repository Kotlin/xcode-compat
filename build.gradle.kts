tasks {

    val plugin by registering(GradleBuild::class) {
        dir = file("xcode-compat")
        tasks = listOf("publish")
    }

    val consumer by registering(GradleBuild::class) {
        dir = file("consumer")
        tasks = listOf("buildForXcode")
    }

    consumer {
        dependsOn(plugin)
    }
}
