val kotlin_version: String by project
val ktor_version: String by project
val junit_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("io.ktor.plugin")
}

group = "ru.moderators.studytogether"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("io.ktor:ktor-serialization-kotlinx-json:${ktor_version}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("junit:junit:$junit_version")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}