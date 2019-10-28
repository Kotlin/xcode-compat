pluginManagement {
    repositories {
        jcenter()
        maven("http://dl.bintray.com/kotlin/kotlin-eap")
        maven { url = uri("../xcode-compat/build/repository") }
    }
}
