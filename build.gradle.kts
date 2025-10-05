plugins {
    kotlin("jvm") version "2.0.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
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
    implementation("net.sandrohc:schematic4j:1.1.0")
}

kotlin {
    jvmToolchain(21)
}


tasks.jar {
    manifest {
        attributes["Main-Class"] = "org.example.MainKt"
    }
}

