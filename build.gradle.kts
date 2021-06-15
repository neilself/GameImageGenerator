import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.21"
}

group = "me.neil"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
    maven("https://jitpack.io")
}

dependencies {
    testImplementation(kotlin("test-junit"))
    implementation("com.sksamuel.scrimage:scrimage-core:4.0.12")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.11.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.+")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
//    implementation("com.github.jitpack:android-example:1.0.1")
//    implementation("com.github.keelar.exprk:ExprK:1.0-SNAPSHOT")
//    implementation("com.github.keelar.exprk:ExprK:")
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}