plugins {
    id("io.ktor.plugin") version "3.4.0"
    kotlin("plugin.serialization") version "2.0.21"
}

repositories {
    mavenCentral()
}

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

kotlin {
    jvmToolchain(21)
}

val ktor_version = "3.4.0"

dependencies {
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-routing:$ktor_version")
    implementation("io.ktor:ktor-server-host-common:$ktor_version")

    implementation("ch.qos.logback:logback-classic:1.4.14")
    testImplementation("io.ktor:ktor-server-test-host:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:2.0.21")
}