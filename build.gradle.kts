import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompileCommon

plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinxSerialization) apply false
    alias(libs.plugins.kotlinxAtomicfu) apply false
    alias(libs.plugins.kotlinxJsPlainObjects) apply false
    alias(libs.plugins.versions) apply true
}

subprojects {
    val compilerArgs = listOf(
        "-Xexpect-actual-classes",
        "-opt-in=kotlin.ExperimentalUnsignedTypes",
        "-opt-in=kotlin.js.ExperimentalJsExport",
        "-opt-in=kotlinx.coroutines.DelicateCoroutinesApi",
        "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
    )

    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            freeCompilerArgs += compilerArgs
        }
    }

    tasks.withType<KotlinJsCompile>().configureEach {
        kotlinOptions {
            freeCompilerArgs += compilerArgs
        }
    }

    tasks.withType<KotlinCompileCommon>().configureEach {
        kotlinOptions {
            freeCompilerArgs += compilerArgs
        }
    }
}

tasks.named<DependencyUpdatesTask>("dependencyUpdates").configure {
    checkForGradleUpdate = true
    outputFormatter = "json"
    outputDir = "build/dependencyUpdates"
    reportfileName = "report"
}

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        isNonStable(candidate.version)
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA", "BETA", "RC").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}
