@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }

    versionCatalogs {
        create("libs") {
            val kotlin = "2.0.0-Beta2"

            version(
                /* alias = */ "kotlin",
                /* version = */ kotlin
            )

            version(
                /* alias = */ "kotlinxCoroutines",
                /* version = */ "1.8.0-RC2"
            )

            version(
                /* alias = */ "kotlinxSerialization",
                /* version = */ "1.6.2"
            )

            version(
                /* alias = */ "ktor",
                /* version = */ "2.3.7"
            )

            plugin(
                /* alias = */ "kotlinMultiplatform",
                /* id = */ "org.jetbrains.kotlin.multiplatform"
            ).versionRef("kotlin")

            plugin(
                /* alias = */ "kotlinxSerialization",
                /* id = */ "org.jetbrains.kotlin.plugin.serialization"
            ).versionRef("kotlin")

            plugin(
                /* alias = */ "versions",
                /* id = */ "com.github.ben-manes.versions"
            ).version("0.50.0")

            library(
                /* alias = */ "koinBom",
                /* group = */ "io.insert-koin",
                /* artifact = */ "koin-bom"
            ).version("3.5.3")

            library(
                /* alias = */ "koinCore",
                /* group = */ "io.insert-koin",
                /* artifact = */ "koin-core"
            ).withoutVersion()

            library(
                /* alias = */ "koinTest",
                /* group = */ "io.insert-koin",
                /* artifact = */ "koin-test"
            ).withoutVersion()

            library(
                /* alias = */ "koinTestJunit5",
                /* group = */ "io.insert-koin",
                /* artifact = */ "koin-test-junit5"
            ).withoutVersion()

            library(
                /* alias = */ "koinKtor",
                /* group = */ "io.insert-koin",
                /* artifact = */ "koin-ktor"
            ).withoutVersion()

            library(
                /* alias = */ "koinLoggerSlf4j",
                /* group = */ "io.insert-koin",
                /* artifact = */ "koin-logger-slf4j"
            ).withoutVersion()

            library(
                /* alias = */ "kotlinTest",
                /* group = */ "org.jetbrains.kotlin",
                /* artifact = */ "kotlin-test"
            ).versionRef("kotlin")

            library(
                /* alias = */ "kotlinTestCommon",
                /* group = */ "org.jetbrains.kotlin",
                /* artifact = */ "kotlin-test-common"
            ).versionRef("kotlin")

            library(
                /* alias = */ "kotlinTestAnnotationsCommon",
                /* group = */ "org.jetbrains.kotlin",
                /* artifact = */ "kotlin-test-annotations-common"
            ).versionRef("kotlin")

            library(
                /* alias = */ "kotlinxCoroutinesTest",
                /* group = */ "org.jetbrains.kotlinx",
                /* artifact = */ "kotlinx-coroutines-test"
            ).versionRef("kotlin")

            library(
                /* alias = */ "kotlinxSerializationJson",
                /* group = */ "org.jetbrains.kotlinx",
                /* artifact = */ "kotlinx-serialization-json"
            ).versionRef("kotlinxSerialization")

            library(
                /* alias = */ "kotlinxSerializationProtobuf",
                /* group = */ "org.jetbrains.kotlinx",
                /* artifact = */ "kotlinx-serialization-protobuf"
            ).versionRef("kotlinxSerialization")

            library(
                /* alias = */ "kotlinxDatetime",
                /* group = */ "org.jetbrains.kotlinx",
                /* artifact = */ "kotlinx-datetime"
            ).version("0.4.0")

            library(
                /* alias = */ "kotlinxCollectionsImmutable",
                /* group = */ "org.jetbrains.kotlinx",
                /* artifact = */ "kotlinx-collections-immutable"
            ).version("0.3.5")

            library(
                /* alias = */ "ktorServerCore",
                /* group = */ "io.ktor",
                /* artifact = */ "ktor-server-core"
            ).versionRef("ktor")

            library(
                /* alias = */ "ktorServerNetty",
                /* group = */ "io.ktor",
                /* artifact = */ "ktor-server-netty"
            ).versionRef("ktor")

            library(
                /* alias = */ "ktorServerHtmlBuilder",
                /* group = */ "io.ktor",
                /* artifact = */ "ktor-server-html-builder"
            ).versionRef("ktor")

            library(
                /* alias = */ "ktorServerContentNegotiation",
                /* group = */ "io.ktor",
                /* artifact = */ "ktor-server-content-negotiation"
            ).versionRef("ktor")

            library(
                /* alias = */ "ktorServerCompression",
                /* group = */ "io.ktor",
                /* artifact = */ "ktor-server-compression"
            ).versionRef("ktor")

            library(
                /* alias = */ "ktorServerWebsockets",
                /* group = */ "io.ktor",
                /* artifact = */ "ktor-server-websockets"
            ).versionRef("ktor")

            library(
                /* alias = */ "ktorSerialization",
                /* group = */ "io.ktor",
                /* artifact = */ "ktor-serialization"
            ).versionRef("ktor")

            library(
                /* alias = */ "ktorSerializationKotlinxJson",
                /* group = */ "io.ktor",
                /* artifact = */ "ktor-serialization-kotlinx-json"
            ).versionRef("ktor")

            library(
                /* alias = */ "ktorWebsockets",
                /* group = */ "io.ktor",
                /* artifact = */ "ktor-websockets"
            ).versionRef("ktor")

            library(
                /* alias = */ "logbackClassic",
                /* group = */ "ch.qos.logback",
                /* artifact = */ "logback-classic"
            ).version("1.4.14")

            library(
                /* alias = */ "ktorClientJs",
                /* group = */ "io.ktor",
                /* artifact = */ "ktor-client-js"
            ).versionRef("ktor")

            library(
                /* alias = */ "ktorClientJsonJs",
                /* group = */ "io.ktor",
                /* artifact = */ "ktor-client-json-js"
            ).versionRef("ktor")

            library(
                /* alias = */ "ktorClientSerializationJs",
                /* group = */ "io.ktor",
                /* artifact = */ "ktor-client-serialization-js"
            ).versionRef("ktor")

            library(
                /* alias = */ "kotlinxCoroutinesCore",
                /* group = */ "org.jetbrains.kotlinx",
                /* artifact = */ "kotlinx-coroutines-core"
            ).versionRef("kotlinxCoroutines")

            library(
                /* alias = */ "kotlinxCoroutinesCoreJs",
                /* group = */ "org.jetbrains.kotlinx",
                /* artifact = */ "kotlinx-coroutines-core-js"
            ).versionRef("kotlinxCoroutines")

            library(
                /* alias = */ "ktorClientCore",
                /* group = */ "io.ktor",
                /* artifact = */ "ktor-client-core"
            ).versionRef("ktor")

            library(
                /* alias = */ "ktorClientLogging",
                /* group = */ "io.ktor",
                /* artifact = */ "ktor-client-logging"
            ).versionRef("ktor")

            library(
                /* alias = */ "ktorClientContentNegotiation",
                /* group = */ "io.ktor",
                /* artifact = */ "ktor-client-content-negotiation"
            ).versionRef("ktor")

            library(
                /* alias = */ "slf4jSimple",
                /* group = */ "org.slf4j",
                /* artifact = */ "slf4j-simple"
            ).version("2.0.7")

            library(
                /* alias = */ "junit",
                /* group = */ "junit",
                /* artifact = */ "junit"
            ).version("4.13.2")
        }
    }
}

rootProject.name = "bolo"

include(":browser")
