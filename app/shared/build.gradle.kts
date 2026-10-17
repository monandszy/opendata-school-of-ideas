import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.openapiGenerator)
}

val openApiOutputDir = layout.buildDirectory.dir("generated/openapi")
val buildEnvOutputDir = layout.buildDirectory.dir("generated/env/src/commonMain/kotlin")

openApiGenerate {
    generatorName.set("kotlin")
    inputSpec.set(rootProject.file("../docs/openapi.yaml").invariantSeparatorsPath)
    outputDir.set(openApiOutputDir.map { it.asFile.invariantSeparatorsPath })
    modelPackage.set("dev.monand.opendata.api.model")
    globalProperties.set(
        mapOf(
            "models" to "",
            "modelDocs" to "false",
            "modelTests" to "false",
        ),
    )
    typeMappings.set(
        mapOf(
            "object" to "JsonObject",
            "AnyType" to "JsonElement",
        ),
    )
    importMappings.set(
        mapOf(
            "JsonObject" to "kotlinx.serialization.json.JsonObject",
            "JsonElement" to "kotlinx.serialization.json.JsonElement",
        ),
    )
    additionalProperties.set(
        mapOf(
            "library" to "multiplatform",
            "dateLibrary" to "string",
        ),
    )
}

val generateBuildEnv by tasks.registering {
    val envFiles = listOf(rootProject.file("../.env"), rootProject.file(".env"))
    val outDir = buildEnvOutputDir
    outputs.dir(outDir)
    notCompatibleWithConfigurationCache("Reads local .env file at execution time")
    doLast {
        val envMap = mutableMapOf<String, String>()
        for (file in envFiles) {
            if (file.exists()) {
                file.readLines().forEach { line ->
                    val trimmed = line.trim()
                    if (trimmed.isNotEmpty() && !trimmed.startsWith("#") && '=' in trimmed) {
                        val idx = trimmed.indexOf('=')
                        val key = trimmed.substring(0, idx).trim()
                        val value =
                            trimmed
                                .substring(idx + 1)
                                .trim()
                                .removeSurrounding("\"")
                                .removeSurrounding("'")
                        envMap[key] = value
                    }
                }
            }
        }
        val apiKey = envMap["WARSAW_API_KEY"] ?: System.getenv("WARSAW_API_KEY").orEmpty()
        val pkgDir = outDir.get().asFile.resolve("dev/monand/opendata")
        pkgDir.mkdirs()
        pkgDir.resolve("BuildEnv.kt").writeText(
            """
            package dev.monand.opendata

            internal object BuildEnv {
                const val WARSAW_API_KEY: String = "$apiKey"
            }
            """.trimIndent() + "\n",
        )
    }
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
            }
        }
        binaries.executable()
    }

    android {
        namespace = "dev.monand.opendata.shared"
        compileSdk =
            libs.versions.android.compileSdk
                .get()
                .toInt()
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.maplibre.android)
        }
        commonMain {
            kotlin.srcDir(openApiOutputDir.map { it.dir("src/commonMain/kotlin") })
            kotlin.srcDir(buildEnvOutputDir)
            dependencies {
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.ui)
                implementation(libs.compose.components.resources)
                implementation(libs.compose.uiToolingPreview)
                implementation(libs.androidx.lifecycle.viewmodelCompose)
                implementation(libs.androidx.lifecycle.runtimeCompose)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
            }
        }
        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}

tasks.withType<GenerateTask>().configureEach {
    notCompatibleWithConfigurationCache("OpenAPI GenerateTask is not compatible with Gradle configuration cache")
}

tasks.withType<KotlinCompilationTask<*>>().configureEach {
    dependsOn("openApiGenerate", generateBuildEnv)
}

tasks.withType<KotlinWebpack>().configureEach {
    notCompatibleWithConfigurationCache("KotlinWebpack Handle is not compatible with Gradle configuration cache")
}
