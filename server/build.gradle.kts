@file:OptIn(ExperimentalKotlinGradlePluginApi::class, ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.kotlinxAtomicfu)
    alias(libs.plugins.kotlinxJsPlainObjects)
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
            implementation(libs.kotlinxCoroutinesCore)
            implementation(libs.kotlinxSerializationJson)
            implementation(libs.kotlinxSerializationProtobuf)
            implementation(libs.kotlinxAtomicfu)
            implementation(libs.koinCore)
            implementation(libs.ktorClientCore)
        }

        commonTest.dependencies {
            implementation(libs.kotlinTest)
            implementation(libs.kotlinTestCommon)
            implementation(libs.kotlinTestAnnotationsCommon)
            implementation(libs.koinTest)
        }

        jvmMain.dependencies {
            implementation(libs.koinKtor)
            implementation(libs.koinLoggerSlf4j)
            implementation(libs.ktorServerCore)
            implementation(libs.ktorServerNetty)
            implementation(libs.ktorServerHtmlBuilder)
            implementation(libs.ktorServerContentNegotiation)
            implementation(libs.ktorServerCompression)
            implementation(libs.ktorServerWebsockets)
            implementation(libs.ktorSerialization)
            implementation(libs.ktorSerializationKotlinxJson)
            implementation(libs.ktorNetworkTlsCertificates)
            implementation(libs.ktorWebsockets)
            implementation(libs.kotlinCss)
            implementation(libs.logbackClassic)
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
