plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.darkfactory.plantpotting"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.darkfactory.plantpotting"
        minSdk = 26
        targetSdk = 34
        versionCode = 3
        versionName = "0.3.0"

        testInstrumentationRunner = "com.darkfactory.plantpotting.HiltTestRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // PLANTPOTTING-0007 — single model-root selection point. The on-device identifier reads
        // its manifest/labels/mapping/model from this assets root. Shipped default is the House
        // Plant Species MobileNetV2 (the swap winner: 6 high-conf correct vs AIY's 1, 10/16 KB
        // coverage vs 2/16, 33ms vs 43ms — see docs/sprints/results/PLANTPOTTING-0007.md). The
        // frozen AIY baseline bundle stays in assets as the regression anchor; point this back to
        // "ml/aiy_plants_v1" to revert. One switch, no scattered conditionals, no second
        // PlantIdentifier implementation.
        buildConfigField("String", "ACTIVE_MODEL_ROOT", "\"ml/house_plant_species_mobilenetv2\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/LICENSE-notice.md"
            excludes += "/META-INF/NOTICE.md"
        }
    }

    androidResources {
        // Keep the on-device model uncompressed so it can be memory-mapped
        // directly from the APK via AssetFileDescriptor.
        noCompress += "tflite"
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
        managedDevices {
            localDevices {
                create("pixel6Api34") {
                    device = "Pixel 6"
                    apiLevel = 34
                    systemImageSource = "aosp"
                }
            }
        }
    }

    lint {
        abortOnError = true
        warningsAsErrors = false
        baseline = file("lint-baseline.xml")
        checkReleaseBuilds = false
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // CameraX
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // Serialization + coroutines
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    // Local on-device persistence (PLANTPOTTING-0010) — typed DataStore, local-file only.
    implementation(libs.androidx.datastore)

    // TensorFlow Lite (on-device inference). PLANTPOTTING-0003 §4.1 — do NOT add
    // tensorflow-lite-task-vision; the hand-rolled InterpreterFacade keeps unit
    // tests JVM-only.
    implementation(libs.tensorflow.lite)
    implementation(libs.tensorflow.lite.support)

    // Unit test
    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.core.testing)
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)

    // Instrumentation test
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.truth)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.espresso.intents)
    androidTestImplementation(libs.androidx.test.uiautomator)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
}

// Fails the build if a production dependency declares a known networking library.
// Required by §8.6 of PLANTPOTTING-0001.
tasks.register("verifyNoNetworking") {
    group = "verification"
    description = "Fails if any production runtime dependency includes a networking library."
    doLast {
        // Narrow allowlist per PLANTPOTTING-0003 §7.4: match only on substrings that
        // are unambiguously networking. `play-services-tasks` is the Tasks API
        // (a coroutine/promise primitive used by TFLite Support), NOT networking,
        // and must not appear here. `okio` is broadly used standalone; `play-services-base`
        // is a generic Play Services foundation. Adding either would cause false positives.
        val forbidden =
            listOf(
                "okhttp",
                "retrofit",
                "firebase",
                "play-services-network",
                "volley",
                "ktor-client-okhttp",
            )
        val config = configurations.getByName("releaseRuntimeClasspath")
        val resolved =
            config.resolvedConfiguration.resolvedArtifacts
                .map { "${it.moduleVersion.id.group}:${it.moduleVersion.id.name}" }
                .sorted()
                .distinct()
        val matches =
            resolved.filter { coord ->
                forbidden.any { needle -> coord.lowercase().contains(needle) }
            }
        if (matches.isNotEmpty()) {
            throw GradleException(
                "verifyNoNetworking: forbidden networking deps in releaseRuntimeClasspath:\n  - " +
                    matches.joinToString("\n  - "),
            )
        }
        // Write a release-runtime-deps audit to disk for the
        // VerifyNoNetworkingRegressionTest unit test and the results doc.
        val auditFile =
            layout.buildDirectory
                .file("verify-no-networking/release-runtime-deps.txt")
                .get()
                .asFile
        auditFile.parentFile.mkdirs()
        auditFile.writeText(resolved.joinToString("\n"))
    }
}

afterEvaluate {
    tasks.named("check").configure {
        dependsOn("verifyNoNetworking")
    }
}
