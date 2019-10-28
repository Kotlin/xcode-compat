package org.jetbrains.kotlin.xcodecompat

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Sync
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.task
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.*
import java.io.File

open class KotlinXcodeExtension(private val project: Project) {
    private fun <T : KotlinNativeTarget> KotlinMultiplatformExtension.presetFromXcode(): AbstractKotlinNativeTargetPreset<T> {
        val sdkName = System.getenv("SDK_NAME") ?: "iosX64"
        val presetName = with(sdkName) {
            when {
                startsWith("iphonesimulator") -> "iosX64"
                startsWith("iphoneos") -> "iosArm64"
                startsWith("macos") -> "macosX64"
                else -> "iosX64"
            }
        }
        return presets.withType<AbstractKotlinNativeTargetPreset<T>>()[presetName]
    }

    private fun NativeBinary.setupTask() {
        val buildType = NativeBuildType.valueOf(System.getenv("CONFIGURATION")?.toUpperCase()
                ?: "DEBUG")
        if (this.buildType == buildType) {
            val dsymTask = project.task<Sync>("buildForXcodeDSYM") {
                dependsOn(linkTask)
                val outputFile = linkTask.outputFile.get()
                val outputDSYM = File(outputFile.parent, outputFile.name + ".dSYM")
                from(outputDSYM)
                into(File(System.getenv("CONFIGURATION_BUILD_DIR"), outputDSYM.name))
            }

            val buildForXcodeTask = project.task<Sync>("buildForXcode") {
                dependsOn(dsymTask)
                val outputFile = linkTask.outputFile.get()
                from(outputFile)
                if (outputKind == NativeOutputKind.FRAMEWORK) {
                    into(File(System.getenv("CONFIGURATION_BUILD_DIR"), outputFile.name))
                }
                else {
                    into(System.getenv("CONFIGURATION_BUILD_DIR"))
                }
            }

            if (this.outputKind == NativeOutputKind.FRAMEWORK) {
                setupFrameworkXcodeProjectTask(buildForXcodeTask, this.baseName)
            }
        }
    }

    private fun setupFrameworkXcodeProjectTask(buildForXcodeTask: Sync, frameworkName: String) {
        project.task<FrameworkXcodeProjectTask>("xcodeproj") {
            this.frameworkName = frameworkName
            this.gradleTaskName = buildForXcodeTask.name

            val generateWrapper = project.findProperty(GENERATE_WRAPPER_PROPERTY)?.toString()?.toBoolean() ?: false
            if (generateWrapper) {
                dependsOn(":wrapper")
            }
        }
    }

    fun KotlinMultiplatformExtension.setupFramework(name: String, configure: Framework.() -> Unit = {}) {
        targetFromPreset(presetFromXcode(), name) {
            binaries.framework {
                configure()
                setupTask()
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

const val GENERATE_WRAPPER_PROPERTY = "org.jetbrains.kotlin.xcodecompat.generate.wrapper"

open class XcodeCompatPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create("xcode", KotlinXcodeExtension::class.java, project)
    }
}
