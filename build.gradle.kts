plugins {
    // client-side
    id("com.android.application") version "9.0.1" apply false
    id("org.jetbrains.kotlin.android") version "2.3.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.0" apply false

    // server-side
    id("org.jetbrains.kotlin.jvm") version "2.3.0" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.0" apply false
    id("io.ktor.plugin") version "3.3.3" apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}