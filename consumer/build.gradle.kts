plugins {
    kotlin("multiplatform") version "1.3.60-eap-76"
    kotlin("xcode-compat") version "0.2.5"
}

kotlin {
    xcode {
        setupApplication("testApp")
    }
}

