import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    application
    kotlin("jvm")
}

group = "dev.robch.bolo"
version = "1.0-SNAPSHOT"

java {
    targetCompatibility = JavaVersion.VERSION_22
}

kotlin {
    compilerOptions {
        extraWarnings.set(true)
        jvmTarget = JvmTarget.JVM_22
    }
}

application {
    mainClass.set("server.MainKt")
}

val generatedOutput: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    implementation(project(":client"))
    generatedOutput(project(":client")) {
        targetConfiguration = "distribution"
    }
    implementation(project(":server"))
    generatedOutput(project(":server")) {
        targetConfiguration = "distribution"
    }
}

val copyGeneratedOutput: TaskProvider<Copy> by tasks.registering(Copy::class) {
    from(generatedOutput)
    into(project.layout.buildDirectory.dir("generatedOutput"))
}

sourceSets.main.configure { resources.srcDir(copyGeneratedOutput) }
