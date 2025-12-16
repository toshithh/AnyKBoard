@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    kotlin("plugin.serialization") version "2.2.21"
}

compose.resources {
    packageOfResClass = "com.toshith.anykboard.generated.resources"
    publicResClass = true
}

kotlin {
    jvm()
    
    sourceSets {

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            implementation(compose.desktop.currentOs)
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
            implementation("org.slf4j:slf4j-simple:2.0.17")

            implementation("io.ktor:ktor-server-core:3.3.3")
            implementation("io.ktor:ktor-server-netty:3.3.3")
            implementation("io.ktor:ktor-server-cio:3.3.3")
            implementation("io.ktor:ktor-server-websockets:3.3.3")


            // QR code
            implementation("com.google.zxing:core:3.5.4")
            implementation("com.google.zxing:javase:3.5.4")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }

    }
}


compose.desktop {
    application {
        mainClass = "com.toshith.anykboard.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "AnyKBoard"
            packageVersion = "1.0.0"

            buildTypes {
                release{
                    proguard {
                        isEnabled = false
                    }
                }
            }

            macOS {
                iconFile.set(project.file("src/jvmMain/composeResources/drawable/akb_mac.icns"))
            }
            windows {
                iconFile.set(project.file("src/jvmMain/composeResources/drawable/akb.ico"))
            }
            linux {
                iconFile.set(project.file("src/jvmMain/composeResources/drawable/akb_logo.png"))
            }
        }
    }
}
