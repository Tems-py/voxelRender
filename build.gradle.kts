plugins {
    kotlin("jvm") version "2.0.20"
    kotlin("plugin.serialization") version "2.0.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "me.tems"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jcodec:jcodec:0.2.5") // or newer
    implementation("org.jcodec:jcodec-javase:0.2.5")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("net.sandrohc:schematic4j:1.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
}

kotlin {
    jvmToolchain(21)
}


tasks.jar {
    manifest {
//        attributes["Main-Class"] = "org.example.MainKt"
        attributes["Implementation-Title"] = "BlockRenderer"
        attributes["Implementation-Version"] = version
    }
    from(sourceSets.main.get().output)
}
