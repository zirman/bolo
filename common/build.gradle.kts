@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.kotlinxJsPlainObjects)
}

java {
    targetCompatibility = JavaVersion.VERSION_22
}

kotlin {
    jvm {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_22
        }

        withJava()
    }

    wasmJs {
        browser {
        }
    }

//    js(IR) {
//        browser {
//        }
//    }

    sourceSets {
        commonMain.dependencies {
            implementation(dependencies.platform(libs.koinBom))
            implementation(dependencies.platform(libs.kotilnxCoroutinesBom))
            implementation(dependencies.platform(libs.kotlinWrappersBom))

            implementation(libs.kotlinxSerializationCore)
            implementation(libs.koinCore)
            implementation(libs.ktorClientCore)
        }

        commonTest.dependencies {
            implementation(libs.kotlinTest)
            implementation(libs.kotlinTestCommon)
            implementation(libs.kotlinTestAnnotationsCommon)
            implementation(libs.koinTest)
        }
    }
}
