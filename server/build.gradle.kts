plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.0.21"
    id("io.ktor.plugin") version "3.4.0"
}

application {
    mainClass = "ru.moderators.studytogether.server.MainKt"
}

group = "ru.moderators.studytogether"
version = "unspecified"

val ktor_version = "3.4.0"
dependencies {
    implementation("org.slf4j:slf4j-api:2.0.17")

    implementation("io.ktor:ktor-server-config-yaml:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-host-common:$ktor_version")

    testImplementation("io.ktor:ktor-server-test-host:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:2.0.21")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}