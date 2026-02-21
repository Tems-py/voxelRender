plugins {
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.serialization") version "2.0.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "me.tems"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
}

kotlin {
    jvmToolchain(25)
}


tasks.jar {
    manifest {
//        attributes["Main-Class"] = "me.tems.MainKt"
        attributes["Implementation-Title"] = "BlockRenderer"
        attributes["Implementation-Version"] = version
    }
    from(sourceSets.main.get().output)
}
