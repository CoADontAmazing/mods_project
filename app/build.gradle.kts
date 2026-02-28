@file:Suppress("PropertyName")


val slf4j_version: String by project
val agp_version: String by project
val ktor_version: String by project
val kotlin_version: String by project
val compose_bom_version: String by project
val core_ktx_version: String by project
val lifecycle_version: String by project
val activity_compose_version: String by project
val junit_version: String by project
val androidx_junit_version: String by project
val espresso_version: String by project


plugins {
    kotlin("plugin.serialization")
    id("io.ktor.plugin")

    id("com.android.application") version "9.0.1"
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.0"
}

android {
    namespace = "ru.moderators.studytogether"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    packaging {
        resources {
            excludes += "META-INF/INDEX.LIST"
        }
    }

    defaultConfig {
        applicationId = "ru.moderators.studytogether"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":api"))

    implementation("org.slf4j:slf4j-android:1.7.9")

    implementation("io.ktor:ktor-serialization-kotlinx-json:${ktor_version}")
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version") // движок
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")

    implementation("androidx.core:core-ktx:$core_ktx_version")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle_version")
    implementation("androidx.activity:activity-compose:$activity_compose_version")
    implementation(platform("androidx.compose:compose-bom:$compose_bom_version"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    androidTestImplementation("androidx.test.ext:junit:$androidx_junit_version")
    androidTestImplementation("androidx.test.espresso:espresso-core:$espresso_version")
    androidTestImplementation(platform("androidx.compose:compose-bom:$compose_bom_version"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)
}