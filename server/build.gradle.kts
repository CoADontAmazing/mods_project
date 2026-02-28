val logback_version: String by project
val kotlin_version: String by project
val ktor_version: String by project

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("io.ktor.plugin")
}

application {
    mainClass = "ru.moderators.studytogether.server.MainKt"
}

group = "ru.moderators.studytogether"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":api"))

    implementation("io.ktor:ktor-server-call-logging:${ktor_version}")
    implementation("ch.qos.logback:logback-classic:${logback_version}")

    implementation("io.ktor:ktor-serialization-kotlinx-json:${ktor_version}")
    implementation("io.ktor:ktor-server-config-yaml:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-host-common:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")

    testImplementation("io.ktor:ktor-server-test-host:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:${kotlin_version}")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}