import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    application
    kotlin("jvm")
}
group = "dev.robch.bolo"
version = "1.0-SNAPSHOT"
java {
    targetCompatibility = JavaVersion.VERSION_21
}
kotlin {
    compilerOptions {
        extraWarnings.set(true)
        jvmTarget = JvmTarget.JVM_21
    }
}
application {
    mainClass.set("server.MainKt")
}
val generatedOutput: Configuration = configurations.create("generatedOutput") {
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
val copyGeneratedOutput: TaskProvider<Copy> = tasks.register("jvmAppOutput", Copy::class) {
    description = "Copy generated output"
    from(generatedOutput)
    into(project.layout.buildDirectory.dir("generatedOutput"))
}
sourceSets.main.configure { resources.srcDir(copyGeneratedOutput) }
