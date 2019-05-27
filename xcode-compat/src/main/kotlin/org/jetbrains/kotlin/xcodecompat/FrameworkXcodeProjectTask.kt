package org.jetbrains.kotlin.xcodecompat

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.wrapper.Wrapper
import java.io.File
import java.security.MessageDigest

open class FrameworkXcodeProjectTask : DefaultTask() {

    @Input
    lateinit var frameworkName: String

    @Input
    lateinit var gradleTaskName: String

    val projectGroup
        @Input get() = project.group.toString()

    @OutputDirectory
    val outputDirectory: File = project.projectDir.resolve("xcodeproj")

    private val xcodeprojDirectory get() = outputDirectory.resolve(frameworkName + ".xcodeproj")
    private val xcodeSourceRoot get() = outputDirectory

    private fun File.toXcodeBuildScriptRelative(): String =
            "\$SRCROOT/${this.toRelativeString(base = xcodeSourceRoot)}"

    @TaskAction
    fun generate() {
        val gradleWrapper = (project.rootProject.tasks.getByName("wrapper") as? Wrapper)?.scriptFile
        require(gradleWrapper != null && gradleWrapper.exists()) {
            """
            The Gradle wrapper is required to run the build from Xcode.

            Please run the same command with `-P$GENERATE_WRAPPER_PROPERTY=true` or run the `:wrapper` task to generate the wrapper manually.

            See details about the wrapper at https://docs.gradle.org/current/userguide/gradle_wrapper.html
            """.trimIndent()
        }

        outputDirectory.mkdirs()
        val infoPlistRelative = "Info.plist"
        outputDirectory.resolve(infoPlistRelative).writeText("""
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
            <plist version="1.0">
            <dict>
                <key>CFBundleDevelopmentRegion</key>
                <string>${'$'}(DEVELOPMENT_LANGUAGE)</string>
                <key>CFBundleExecutable</key>
                <string>${'$'}(EXECUTABLE_NAME)</string>
                <key>CFBundleIdentifier</key>
                <string>${'$'}(PRODUCT_BUNDLE_IDENTIFIER)</string>
                <key>CFBundleInfoDictionaryVersion</key>
                <string>6.0</string>
                <key>CFBundleName</key>
                <string>${'$'}(PRODUCT_NAME)</string>
                <key>CFBundlePackageType</key>
                <string>FMWK</string>
                <key>CFBundleShortVersionString</key>
                <string>1.0</string>
                <key>CFBundleVersion</key>
                <string>${'$'}(CURRENT_PROJECT_VERSION)</string>
            </dict>
            </plist>
        """.trimIndent())

        val gradleTaskFqName = buildString {
            append(project.path)
            if (!endsWith(':')) append(':')
            append(gradleTaskName)
        }

        val gradleWrapperForScript = gradleWrapper.toXcodeBuildScriptRelative()
        val projectDirForScript = project.projectDir.toXcodeBuildScriptRelative()

        val bundleIdentifier: String = projectGroup.let { if (it.isEmpty()) frameworkName else "$it.$frameworkName" }

        // Trivial effort to make IDs unique but stable:
        val ids = generateSequence(0) { it + 1 }
                .map { "$frameworkName:$it".sha256().substring(0, 24) }
                .distinct()
                .iterator()

        val mainGroupID = ids.next()
        val productsGroupID = ids.next()
        val frameworkFileID = ids.next()
        val targetID = ids.next()
        val scriptBuildPhaseID = ids.next()
        val targetBuildConfigurationListID = ids.next()
        val targetDebugBuildConfigurationID = ids.next()
        val targetReleaseBuildConfigurationID = ids.next()
        val projectBuildConfigurationListID = ids.next()
        val projectDebugBuildConfigurationID = ids.next()
        val projectReleaseBuildConfigurationID = ids.next()
        val projectID = ids.next()

        xcodeprojDirectory.mkdirs()
        xcodeprojDirectory.resolve("project.pbxproj").writeText("""
            // !${'$'}*UTF8*${'$'}!
            {
                archiveVersion = 1;
                classes = {
                };
                objectVersion = 46;
                objects = {

            /* Begin PBXFileReference section */
                    $frameworkFileID /* $frameworkName.framework */ = {isa = PBXFileReference; explicitFileType = wrapper.framework; includeInIndex = 0; path = $frameworkName.framework; sourceTree = BUILT_PRODUCTS_DIR; };
            /* End PBXFileReference section */

            /* Begin PBXGroup section */
                    $mainGroupID = {
                        isa = PBXGroup;
                        children = (
                            $productsGroupID /* Products */,
                        );
                        sourceTree = "<group>";
                    };
                    $productsGroupID /* Products */ = {
                        isa = PBXGroup;
                        children = (
                            $frameworkFileID /* $frameworkName.framework */,
                        );
                        name = Products;
                        sourceTree = "<group>";
                    };
            /* End PBXGroup section */

            /* Begin PBXNativeTarget section */
                    $targetID /* $frameworkName */ = {
                        isa = PBXNativeTarget;
                        buildConfigurationList = $targetBuildConfigurationListID /* Build configuration list for PBXNativeTarget "$frameworkName" */;
                        buildPhases = (
                            $scriptBuildPhaseID /* Compile Kotlin/Native */,
                        );
                        buildRules = (
                        );
                        dependencies = (
                        );
                        name = $frameworkName;
                        productName = $frameworkName;
                        productReference = $frameworkFileID /* $frameworkName.framework */;
                        productType = "com.apple.product-type.framework";
                    };
            /* End PBXNativeTarget section */

            /* Begin PBXProject section */
                    $projectID /* Project object */ = {
                        isa = PBXProject;
                        attributes = {
                            ORGANIZATIONNAME = Kotlin/Native;
                        };
                        buildConfigurationList = $projectBuildConfigurationListID /* Build configuration list for PBXProject "$frameworkName" */;
                        compatibilityVersion = "Xcode 3.2";
                        developmentRegion = English;
                        hasScannedForEncodings = 0;
                        knownRegions = (
                            en,
                        );
                        mainGroup = $mainGroupID;
                        productRefGroup = $productsGroupID /* Products */;
                        projectDirPath = "";
                        projectRoot = "";
                        targets = (
                            $targetID /* $frameworkName */,
                        );
                    };
            /* End PBXProject section */

            /* Begin PBXShellScriptBuildPhase section */
                    $scriptBuildPhaseID /* Compile Kotlin/Native */ = {
                        isa = PBXShellScriptBuildPhase;
                        buildActionMask = 2147483647;
                        files = (
                        );
                        inputPaths = (
                        );
                        name = "Compile Kotlin/Native";
                        outputPaths = (
                        );
                        runOnlyForDeploymentPostprocessing = 0;
                        shellPath = /bin/sh;
                        shellScript = "\"$gradleWrapperForScript\" -p \"$projectDirForScript\" \"$gradleTaskFqName\"\n";
                    };
            /* End PBXShellScriptBuildPhase section */

            /* Begin XCBuildConfiguration section */
                    $targetDebugBuildConfigurationID /* Debug */ = {
                        isa = XCBuildConfiguration;
                        buildSettings = {
                            CODE_SIGN_IDENTITY = "";
                            DYLIB_INSTALL_NAME_BASE = "@rpath";
                            INFOPLIST_FILE = $infoPlistRelative;
                            INSTALL_PATH = "${'$'}(LOCAL_LIBRARY_DIR)/Frameworks";
                            PRODUCT_BUNDLE_IDENTIFIER = $bundleIdentifier;
                            PRODUCT_MODULE_NAME = "_${'$'}(PRODUCT_NAME:c99extidentifier)";
                            PRODUCT_NAME = "${'$'}(TARGET_NAME:c99extidentifier)";
                            SKIP_INSTALL = YES;
                            TARGETED_DEVICE_FAMILY = "1,2";
                            VALID_ARCHS = arm64;
                        };
                        name = Debug;
                    };
                    $projectReleaseBuildConfigurationID /* Release */ = {
                        isa = XCBuildConfiguration;
                        buildSettings = {
                            ALWAYS_SEARCH_USER_PATHS = NO;
                            CLANG_ANALYZER_NONNULL = YES;
                            CLANG_ANALYZER_NUMBER_OBJECT_CONVERSION = YES_AGGRESSIVE;
                            CLANG_CXX_LANGUAGE_STANDARD = "gnu++14";
                            CLANG_CXX_LIBRARY = "libc++";
                            CLANG_ENABLE_MODULES = YES;
                            CLANG_ENABLE_OBJC_ARC = YES;
                            CLANG_ENABLE_OBJC_WEAK = YES;
                            CLANG_WARN_BLOCK_CAPTURE_AUTORELEASING = YES;
                            CLANG_WARN_BOOL_CONVERSION = YES;
                            CLANG_WARN_COMMA = YES;
                            CLANG_WARN_CONSTANT_CONVERSION = YES;
                            CLANG_WARN_DEPRECATED_OBJC_IMPLEMENTATIONS = YES;
                            CLANG_WARN_DIRECT_OBJC_ISA_USAGE = YES_ERROR;
                            CLANG_WARN_DOCUMENTATION_COMMENTS = YES;
                            CLANG_WARN_EMPTY_BODY = YES;
                            CLANG_WARN_ENUM_CONVERSION = YES;
                            CLANG_WARN_INFINITE_RECURSION = YES;
                            CLANG_WARN_INT_CONVERSION = YES;
                            CLANG_WARN_NON_LITERAL_NULL_CONVERSION = YES;
                            CLANG_WARN_OBJC_IMPLICIT_RETAIN_SELF = YES;
                            CLANG_WARN_OBJC_LITERAL_CONVERSION = YES;
                            CLANG_WARN_OBJC_ROOT_CLASS = YES_ERROR;
                            CLANG_WARN_RANGE_LOOP_ANALYSIS = YES;
                            CLANG_WARN_STRICT_PROTOTYPES = YES;
                            CLANG_WARN_SUSPICIOUS_MOVE = YES;
                            CLANG_WARN_UNGUARDED_AVAILABILITY = YES_AGGRESSIVE;
                            CLANG_WARN_UNREACHABLE_CODE = YES;
                            CLANG_WARN__DUPLICATE_METHOD_MATCH = YES;
                            CODE_SIGN_IDENTITY = "iPhone Developer";
                            COPY_PHASE_STRIP = NO;
                            DEBUG_INFORMATION_FORMAT = "dwarf-with-dsym";
                            ENABLE_NS_ASSERTIONS = NO;
                            ENABLE_STRICT_OBJC_MSGSEND = YES;
                            GCC_C_LANGUAGE_STANDARD = gnu11;
                            GCC_NO_COMMON_BLOCKS = YES;
                            GCC_WARN_64_TO_32_BIT_CONVERSION = YES;
                            GCC_WARN_ABOUT_RETURN_TYPE = YES_ERROR;
                            GCC_WARN_UNDECLARED_SELECTOR = YES;
                            GCC_WARN_UNINITIALIZED_AUTOS = YES_AGGRESSIVE;
                            GCC_WARN_UNUSED_FUNCTION = YES;
                            GCC_WARN_UNUSED_VARIABLE = YES;
                            IPHONEOS_DEPLOYMENT_TARGET = 12.2;
                            MTL_ENABLE_DEBUG_INFO = NO;
                            MTL_FAST_MATH = YES;
                            SDKROOT = iphoneos;
                            VALIDATE_PRODUCT = YES;
                        };
                        name = Release;
                    };
                    $targetReleaseBuildConfigurationID /* Release */ = {
                        isa = XCBuildConfiguration;
                        buildSettings = {
                            CODE_SIGN_IDENTITY = "";
                            DYLIB_INSTALL_NAME_BASE = "@rpath";
                            INFOPLIST_FILE = $infoPlistRelative;
                            INSTALL_PATH = "${'$'}(LOCAL_LIBRARY_DIR)/Frameworks";
                            PRODUCT_BUNDLE_IDENTIFIER = $bundleIdentifier;
                            PRODUCT_MODULE_NAME = "_${'$'}(PRODUCT_NAME:c99extidentifier)";
                            PRODUCT_NAME = "${'$'}(TARGET_NAME:c99extidentifier)";
                            SKIP_INSTALL = YES;
                            TARGETED_DEVICE_FAMILY = "1,2";
                            VALID_ARCHS = arm64;
                        };
                        name = Release;
                    };
                    $projectDebugBuildConfigurationID /* Debug */ = {
                        isa = XCBuildConfiguration;
                        buildSettings = {
                            ALWAYS_SEARCH_USER_PATHS = NO;
                            CLANG_ANALYZER_NONNULL = YES;
                            CLANG_ANALYZER_NUMBER_OBJECT_CONVERSION = YES_AGGRESSIVE;
                            CLANG_CXX_LANGUAGE_STANDARD = "gnu++14";
                            CLANG_CXX_LIBRARY = "libc++";
                            CLANG_ENABLE_MODULES = YES;
                            CLANG_ENABLE_OBJC_ARC = YES;
                            CLANG_ENABLE_OBJC_WEAK = YES;
                            CLANG_WARN_BLOCK_CAPTURE_AUTORELEASING = YES;
                            CLANG_WARN_BOOL_CONVERSION = YES;
                            CLANG_WARN_COMMA = YES;
                            CLANG_WARN_CONSTANT_CONVERSION = YES;
                            CLANG_WARN_DEPRECATED_OBJC_IMPLEMENTATIONS = YES;
                            CLANG_WARN_DIRECT_OBJC_ISA_USAGE = YES_ERROR;
                            CLANG_WARN_DOCUMENTATION_COMMENTS = YES;
                            CLANG_WARN_EMPTY_BODY = YES;
                            CLANG_WARN_ENUM_CONVERSION = YES;
                            CLANG_WARN_INFINITE_RECURSION = YES;
                            CLANG_WARN_INT_CONVERSION = YES;
                            CLANG_WARN_NON_LITERAL_NULL_CONVERSION = YES;
                            CLANG_WARN_OBJC_IMPLICIT_RETAIN_SELF = YES;
                            CLANG_WARN_OBJC_LITERAL_CONVERSION = YES;
                            CLANG_WARN_OBJC_ROOT_CLASS = YES_ERROR;
                            CLANG_WARN_RANGE_LOOP_ANALYSIS = YES;
                            CLANG_WARN_STRICT_PROTOTYPES = YES;
                            CLANG_WARN_SUSPICIOUS_MOVE = YES;
                            CLANG_WARN_UNGUARDED_AVAILABILITY = YES_AGGRESSIVE;
                            CLANG_WARN_UNREACHABLE_CODE = YES;
                            CLANG_WARN__DUPLICATE_METHOD_MATCH = YES;
                            CODE_SIGN_IDENTITY = "iPhone Developer";
                            COPY_PHASE_STRIP = NO;
                            DEBUG_INFORMATION_FORMAT = dwarf;
                            ENABLE_STRICT_OBJC_MSGSEND = YES;
                            ENABLE_TESTABILITY = YES;
                            GCC_C_LANGUAGE_STANDARD = gnu11;
                            GCC_DYNAMIC_NO_PIC = NO;
                            GCC_NO_COMMON_BLOCKS = YES;
                            GCC_OPTIMIZATION_LEVEL = 0;
                            GCC_PREPROCESSOR_DEFINITIONS = (
                                "DEBUG=1",
                                "${'$'}(inherited)",
                            );
                            GCC_WARN_64_TO_32_BIT_CONVERSION = YES;
                            GCC_WARN_ABOUT_RETURN_TYPE = YES_ERROR;
                            GCC_WARN_UNDECLARED_SELECTOR = YES;
                            GCC_WARN_UNINITIALIZED_AUTOS = YES_AGGRESSIVE;
                            GCC_WARN_UNUSED_FUNCTION = YES;
                            GCC_WARN_UNUSED_VARIABLE = YES;
                            IPHONEOS_DEPLOYMENT_TARGET = 12.2;
                            MTL_ENABLE_DEBUG_INFO = INCLUDE_SOURCE;
                            MTL_FAST_MATH = YES;
                            ONLY_ACTIVE_ARCH = YES;
                            SDKROOT = iphoneos;
                        };
                        name = Debug;
                    };
            /* End XCBuildConfiguration section */

            /* Begin XCConfigurationList section */
                    $targetBuildConfigurationListID /* Build configuration list for PBXNativeTarget "$frameworkName" */ = {
                        isa = XCConfigurationList;
                        buildConfigurations = (
                            $targetDebugBuildConfigurationID /* Debug */,
                            $targetReleaseBuildConfigurationID /* Release */,
                        );
                        defaultConfigurationIsVisible = 0;
                    };
                    $projectBuildConfigurationListID /* Build configuration list for PBXProject "$frameworkName" */ = {
                        isa = XCConfigurationList;
                        buildConfigurations = (
                            $projectDebugBuildConfigurationID /* Debug */,
                            $projectReleaseBuildConfigurationID /* Release */,
                        );
                        defaultConfigurationIsVisible = 0;
                        defaultConfigurationName = Release;
                    };
            /* End XCConfigurationList section */
                };
                rootObject = $projectID /* Project object */;
            }
        """.trimIndent())
    }
}

fun String.sha256(): String =
        sha256MessageDigest.digest(this.toByteArray()).joinToString("") { "%02X".format(it) }

private val sha256MessageDigest = MessageDigest.getInstance("SHA-256")
