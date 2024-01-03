@file:OptIn(org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl::class)

plugins {
    application
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinxSerialization)
}

group = "dev.robch"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("me.robch.application.MainKt")
}

kotlin {
    jvm {
        jvmToolchain(jdkVersion = 17)

        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }

        withJava()

        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    js(IR) {
        moduleName = "jsClient"
        useEsModules()

        compilations.all {
            kotlinOptions {
                useEsClasses = true
            }
        }

        browser {
            commonWebpackConfig {
                outputFileName = "bolo.js"
            }
        }

        binaries.executable()
    }

    wasmJs {
        moduleName = "wasmClient"

        browser {
            browser {
                commonWebpackConfig {
                    outputFileName = "boloWasm.js"
                }
            }
        }

        binaries.executable()
    }

    dependencies {
        implementation(project.dependencies.platform(libs.koinBom))
        compileOnly(libs.koinCore)
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinxCoroutinesCore)
                implementation(libs.kotlinxSerializationProtobuf)
                implementation(project.dependencies.platform(libs.koinBom))
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlinTest)
                implementation(libs.kotlinTestCommon)
                implementation(libs.kotlinTestAnnotationsCommon)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(libs.koinCore)
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
                implementation(libs.ktorWebsockets)
                implementation(libs.logbackClassic)
            }
        }

        val wasmJsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-wasm-js:1.8.0-RC2-wasm0")
                implementation("io.ktor:ktor-client-core-wasm-js:3.0.0-wasm2")
                implementation("io.ktor:ktor-client-js-wasm-js:3.0.0-wasm2")
                implementation("io.ktor:ktor-client-json-wasm-js:3.0.0-wasm2")
                implementation("io.ktor:ktor-client-serialization-wasm-js:3.0.0-wasm2")
                implementation("io.ktor:ktor-client-websockets-wasm-js:3.0.0-wasm2")
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(libs.kotlinxCoroutinesCoreJs)
                implementation(libs.koinCore)
                implementation(libs.ktorClientJs)
                implementation(libs.ktorClientJsonJs)
                implementation(libs.ktorClientSerializationJs)
            }
        }
    }
}

tasks.named<Copy>("jvmProcessResources") {
    from(tasks.named<Copy>("jsBrowserDistribution"))
    from(tasks.named<Copy>("wasmJsBrowserDistribution"))
}
