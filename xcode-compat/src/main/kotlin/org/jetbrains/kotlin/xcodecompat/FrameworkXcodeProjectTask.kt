package org.jetbrains.kotlin.xcodecompat

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.wrapper.Wrapper
import java.io.File

open class FrameworkXcodeProjectTask : DefaultTask() {

    @Input
    lateinit var frameworkName: String

    @Input
    lateinit var gradleTaskName: String

    val projectGroup
        @Input get() = project.group.toString()

    @OutputDirectory
    val outputDirectory: File = project.projectDir.resolve("xcodeproj")

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

        val xcodeprojDirectory = outputDirectory.resolve(frameworkName + ".xcodeproj")
        val bundleIdentifier: String = projectGroup.let { if (it.isEmpty()) frameworkName else "$it.$frameworkName" }

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
                    62E985139ABA658F4C6C577F /* $frameworkName.framework */ = {isa = PBXFileReference; explicitFileType = wrapper.framework; includeInIndex = 0; path = $frameworkName.framework; sourceTree = BUILT_PRODUCTS_DIR; };
            /* End PBXFileReference section */

            /* Begin PBXGroup section */
                    62E98571159A645898E04E2C = {
                        isa = PBXGroup;
                        children = (
                            62E98FF247DD31C30C7F51BC /* Products */,
                        );
                        sourceTree = "<group>";
                    };
                    62E98FF247DD31C30C7F51BC /* Products */ = {
                        isa = PBXGroup;
                        children = (
                            62E985139ABA658F4C6C577F /* $frameworkName.framework */,
                        );
                        name = Products;
                        sourceTree = "<group>";
                    };
            /* End PBXGroup section */

            /* Begin PBXNativeTarget section */
                    62E987E2F8F8FF10F3D97DD0 /* $frameworkName */ = {
                        isa = PBXNativeTarget;
                        buildConfigurationList = 62E9800271F3022AE71848BC /* Build configuration list for PBXNativeTarget "$frameworkName" */;
                        buildPhases = (
                            62E98962EFB3E11B7F39A707 /* Compile Kotlin/Native */,
                        );
                        buildRules = (
                        );
                        dependencies = (
                        );
                        name = $frameworkName;
                        productName = $frameworkName;
                        productReference = 62E985139ABA658F4C6C577F /* $frameworkName.framework */;
                        productType = "com.apple.product-type.framework";
                    };
            /* End PBXNativeTarget section */

            /* Begin PBXProject section */
                    62E9824CD21C2C5BD25EFCA3 /* Project object */ = {
                        isa = PBXProject;
                        attributes = {
                            ORGANIZATIONNAME = Kotlin/Native;
                        };
                        buildConfigurationList = 62E9828C6A9CD86C92ADBAAB /* Build configuration list for PBXProject "$frameworkName" */;
                        compatibilityVersion = "Xcode 3.2";
                        developmentRegion = English;
                        hasScannedForEncodings = 0;
                        knownRegions = (
                            en,
                        );
                        mainGroup = 62E98571159A645898E04E2C;
                        productRefGroup = 62E98FF247DD31C30C7F51BC /* Products */;
                        projectDirPath = "";
                        projectRoot = "";
                        targets = (
                            62E987E2F8F8FF10F3D97DD0 /* $frameworkName */,
                        );
                    };
            /* End PBXProject section */

            /* Begin PBXShellScriptBuildPhase section */
                    62E98962EFB3E11B7F39A707 /* Compile Kotlin/Native */ = {
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
                        shellScript = "\"$gradleWrapper\" -p \"${project.projectDir}\" \"$gradleTaskName\"\n";
                    };
            /* End PBXShellScriptBuildPhase section */

            /* Begin XCBuildConfiguration section */
                    62E982B13330DD0D28B667BB /* Debug */ = {
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
                    62E98522A6998905313A73C1 /* Release */ = {
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
                    62E98BEAF3266BD5F933191F /* Release */ = {
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
                    62E98F0EC52020C85A402CCB /* Debug */ = {
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
                    62E9800271F3022AE71848BC /* Build configuration list for PBXNativeTarget "$frameworkName" */ = {
                        isa = XCConfigurationList;
                        buildConfigurations = (
                            62E982B13330DD0D28B667BB /* Debug */,
                            62E98BEAF3266BD5F933191F /* Release */,
                        );
                        defaultConfigurationIsVisible = 0;
                    };
                    62E9828C6A9CD86C92ADBAAB /* Build configuration list for PBXProject "$frameworkName" */ = {
                        isa = XCConfigurationList;
                        buildConfigurations = (
                            62E98F0EC52020C85A402CCB /* Debug */,
                            62E98522A6998905313A73C1 /* Release */,
                        );
                        defaultConfigurationIsVisible = 0;
                        defaultConfigurationName = Release;
                    };
            /* End XCConfigurationList section */
                };
                rootObject = 62E9824CD21C2C5BD25EFCA3 /* Project object */;
            }
        """.trimIndent())
    }
}