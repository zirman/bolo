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

//        if (project.gradle.startParameter.taskNames.find { it.contains("run") } != null) {
//            applyBinaryen {
//                binaryenArgs = mutableListOf(
//                    "--enable-nontrapping-float-to-int",
//                    "--enable-gc",
//                    "--enable-reference-types",
//                    "--enable-exception-handling",
//                    "--enable-bulk-memory",
//                    "--inline-functions-with-loops",
//                    "--traps-never-happen",
//                    "--fast-math",
//                    "--closed-world",
//                    "--metrics",
//                    "-O3", "--gufa", "--metrics",
//                    "-O3", "--gufa", "--metrics",
//                    "-O3", "--gufa", "--metrics",
//                )
//            }
//        }
    }

    dependencies {
        implementation(project.dependencies.platform(libs.koinBom))
        compileOnly(libs.koinCore)
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinxCoroutinesCore)
            implementation(libs.kotlinxSerializationJson)
            implementation(libs.kotlinxSerializationProtobuf)
            implementation(libs.ktorClientCore)
            implementation(project.dependencies.platform(libs.koinBom))
        }

        commonTest.dependencies {
            implementation(libs.kotlinTest)
            implementation(libs.kotlinTestCommon)
            implementation(libs.kotlinTestAnnotationsCommon)
        }

        jvmMain.dependencies {
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

        jsMain.dependencies {
            implementation(libs.kotlinxCoroutinesCoreJs)
            implementation(libs.koinCore)
            implementation(libs.ktorClientJs)
            implementation(libs.ktorClientJsonJs)
            implementation(libs.ktorClientSerializationJs)
        }

        wasmJsMain.dependencies {
            implementation(libs.kotlinxCoroutinesCoreWasmJs)
            implementation(libs.ktorClientJsWasmJs)
            implementation(libs.ktorClientJsonWasmJs)
            implementation(libs.ktorClientSerializationWasmJs)
            implementation(libs.ktorClientWebsocketsWasmJs)
        }
    }
}

tasks.named<Copy>("jvmProcessResources") {
    from(tasks.named<Copy>("jsBrowserDistribution"))
    from(tasks.named<Copy>("wasmJsBrowserDistribution"))
}
