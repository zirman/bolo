@file:OptIn(ExperimentalKotlinGradlePluginApi::class, ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

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

    wasmJs {
        moduleName = "wasmClient"
        useEsModules()

        compilerOptions {
            useEsClasses = true
        }

        browser {
            commonWebpackConfig {
                mode = KotlinWebpackConfig.Mode.DEVELOPMENT
                outputFileName = "boloWasm.js"
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

//    js(IR) {
//        moduleName = "jsClient"
//        useEsModules()
//
//        compilerOptions {
//            useEsClasses = true
//        }
//
//        browser {
//            commonWebpackConfig {
//                mode = KotlinWebpackConfig.Mode.DEVELOPMENT
//                outputFileName = "bolo.js"
//            }
//        }
//
//        binaries.executable()
//    }

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

//        jvmMain.dependencies {
//        }

        wasmJsMain.dependencies {
            implementation(libs.kotlinxCoroutinesCoreWasmJs)
            implementation(libs.ktorClientJsWasmJs)
            implementation(libs.ktorClientJsonWasmJs)
            implementation(libs.ktorClientSerializationWasmJs)
            implementation(libs.ktorClientWebsocketsWasmJs)
        }

//        jsMain.dependencies {
//            implementation(libs.kotlinxCoroutinesCoreJs)
//            implementation(libs.ktorClientJs)
//            implementation(libs.ktorClientJsonJs)
//            implementation(libs.ktorClientSerializationJs)
//        }
    }
}

tasks.named<Copy>("jvmProcessResources") {
    from(tasks.named<Copy>("wasmJsBrowserDistribution"))
    from("build/compileSync/wasmJs/main/productionExecutable/kotlin/wasmClient.wasm")
    from("build/compileSync/wasmJs/main/productionExecutable/kotlin/wasmClient.wasm.map")
//    from(tasks.named<Copy>("jsBrowserDistribution"))
//    from("build/compileSync/wasmJs/main/productionExecutable/optimized/wasmClient.wasm")
//    from("build/compileSync/wasmJs/main/productionExecutable/optimized/wasmClient.wasm.map")
}

val distribution: NamedDomainObjectProvider<Configuration> by configurations.registering {
    isCanBeConsumed = true
    isCanBeResolved = false
}

artifacts {
    add(distribution.name, tasks.named("jvmProcessResources"))
}
