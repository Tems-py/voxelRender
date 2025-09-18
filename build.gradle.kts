plugins {
    kotlin("jvm") version "2.0.20"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jcodec:jcodec:0.2.5") // or newer
    implementation("org.jcodec:jcodec-javase:0.2.5")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
}

kotlin {
    jvmToolchain(21)
}