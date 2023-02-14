import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.0"
    application

    id("maven-publish")
    id("java-library")
}

group = "com.github.wasteix"
version = "1.0.1"

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}
repositories {
    mavenCentral()
}

publishing {
    repositories {
        mavenCentral()
    }

    publications {
        create<MavenPublication>("maven") {
            artifactId = "user-vk-commandlib"

            from(components["java"])
        }
    }

    dependencies {
        api("com.vk.api:sdk:1.0.14")

        api("com.google.code.gson:gson:2.10.1")
        api("com.google.guava:guava:31.1-jre")

        implementation(kotlin("stdlib-jdk8"))
    }
}

kotlin {
    jvmToolchain(8)
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}