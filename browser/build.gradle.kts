import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile

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
        moduleName = "client"
        useEsModules()
        browser {
            commonWebpackConfig {
                outputFileName = "bolo.js"
            }
        }
        binaries.executable()
    }

    wasmJs {
        moduleName = "client"
        browser()
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

        val jsMain by getting {
            dependencies {
                implementation(libs.kotlinxCoroutinesCoreJs)
                implementation(libs.koinCore)
                implementation(libs.ktorClientJs)
                implementation(libs.ktorClientJsonJs)
                implementation(libs.ktorClientSerializationJs)
            }
        }

        val wasmJsMain by getting {
            dependencies {
            }
        }
    }
}

tasks.named<Copy>("jvmProcessResources") {
    from(tasks.named("jsBrowserDistribution"))
}

tasks.named<JavaExec>("run") {
    dependsOn(tasks.named<Jar>("jvmJar"))
    classpath(tasks.named<Jar>("jvmJar"))
}

// Enables ES6 classes generation
tasks.withType<KotlinJsCompile>().configureEach {
    kotlinOptions {
        useEsClasses = true
    }
}
