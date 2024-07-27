plugins {
    application
    kotlin("jvm")
}

group = "dev.robch.bolo"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("dev.robch.bolo.MainKt")
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
}

val copyGeneratedOutput: TaskProvider<Copy> by tasks.registering(Copy::class) {
    from(generatedOutput)
    into(project.layout.buildDirectory.dir("generatedOutput"))
}

sourceSets.main.configure { resources.srcDir(copyGeneratedOutput) }
