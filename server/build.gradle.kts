@file:OptIn(ExperimentalKotlinGradlePluginApi::class, ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
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

    sourceSets {
        commonMain.dependencies {
            implementation(dependencies.platform(libs.koinBom))
            implementation(dependencies.platform(libs.kotilnxCoroutinesBom))
            implementation(dependencies.platform(libs.kotlinWrappersBom))

            implementation(project(":common"))
            implementation(libs.kotlinCss)
            implementation(libs.kotlinxSerializationProtobuf)
            implementation(libs.koinCore)
            implementation(libs.ktorServerNetty)
            implementation(libs.ktorServerHtmlBuilder)
            implementation(libs.ktorServerContentNegotiation)
            implementation(libs.ktorServerCompression)
            implementation(libs.ktorServerWebsockets)
            implementation(libs.ktorSerializationKotlinxJson)
            implementation(libs.ktorNetworkTlsCertificates)
            implementation(libs.logbackClassic)
        }

        commonTest.dependencies {
            implementation(libs.kotlinTest)
            implementation(libs.kotlinTestCommon)
            implementation(libs.kotlinTestAnnotationsCommon)
            implementation(libs.koinTest)
        }
    }
}

val distribution: NamedDomainObjectProvider<Configuration> by configurations.registering {
    isCanBeConsumed = true
    isCanBeResolved = false
}

artifacts {
    add(distribution.name, tasks.named("jvmProcessResources"))
}
