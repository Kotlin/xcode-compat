package org.jetbrains.kotlin.xcodecompat

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Sync
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.task
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.*


open class KotlinXcodeExtension(private val project: Project) {
    private fun KotlinMultiplatformExtension.presetFromXcode(): KotlinNativeTargetPreset {
        val sdkName = System.getenv("SDK_NAME") ?: "iosX64"
        val presetName = with(sdkName) {
            when {
                startsWith("iphonesimulator") -> "iosX64"
                startsWith("iphoneos") -> "iosArm64"
                startsWith("macos") -> "macosX64"
                else -> "iosX64"
            }
        }
        return presets.withType<KotlinNativeTargetPreset>()[presetName]
    }

    private fun NativeBinary.setupTask() {
        val buildType = NativeBuildType.valueOf(System.getenv("CONFIGURATION")?.toUpperCase()
                ?: "DEBUG")
        if (this.buildType == buildType) {
            project.task<Sync>("buildForXcode") {
                dependsOn(linkTask)
                from(linkTask.outputFile.get().parentFile)
                into(System.getenv("CONFIGURATION_BUILD_DIR"))
            }
        }
    }

    fun KotlinMultiplatformExtension.setupFramework(name: String, configure: Framework.() -> Unit = {}) {
        targetFromPreset(presetFromXcode(), name) {
            binaries.framework {
                configure()
                setupTask()
                compilations["main"].cinterops {
                    libcurl
                }
            }
        }
    }

    fun KotlinMultiplatformExtension.setupApplication(name: String, configure: Executable.() -> Unit = {}) {
        targetFromPreset(presetFromXcode(), name) {
            binaries.executable {
                configure()
                setupTask()
            }
        }
    }
}

open class XcodeCompatPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create("xcode", KotlinXcodeExtension::class.java, project)
    }
}
