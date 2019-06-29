import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.21"
}

group = "com.example.vtlfokin"
version = "1.0-SNAPSHOT"

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    compile("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.0-M1")
    compile("no.tornado:tornadofx:1.7.19")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}