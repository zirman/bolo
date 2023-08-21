plugins {
    application
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.versions)
}

group = "dev.robch"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("me.robch.application.ServerKt")
}

kotlin {
    jvm {
        jvmToolchain(jdkVersion = 17)

        compilations.all {
            kotlinOptions.jvmTarget = "17"
        }

        withJava()

        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    js(IR) {
        binaries.executable()

        browser {
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinxSerializationProtobuf)
                implementation(libs.kotlinxCoroutinesCore)
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
                implementation(libs.ktorClientJs)
                implementation(libs.ktorClientJsonJs)
                implementation(libs.ktorClientSerializationJs)
                implementation(libs.kotlinxCoroutinesCore)
                implementation(libs.kotlinxCoroutinesCoreJs)
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
