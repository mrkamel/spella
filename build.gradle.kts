/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Kotlin application project to get you started.
 */

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "1.5.10"
    id("com.github.johnrengelman.shadow") version "7.0.0"

    // Apply the application plugin to add support for building a CLI application.
    application
}

repositories {
    mavenCentral()
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")

    testImplementation("io.kotest:kotest-runner-junit5:4.6.0")
    testImplementation("io.kotest:kotest-assertions-core:4.6.0")
    testImplementation("io.ktor:ktor-server-test-host:1.6.0")
    testImplementation("io.mockk:mockk:1.11.0")
    testImplementation("com.google.code.gson:gson:1.11.0")

    implementation("io.ktor:ktor-server-core:1.6.0")
    implementation("io.ktor:ktor-server-cio:1.6.0")
    implementation("io.ktor:ktor-gson:1.6.0")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("org.slf4j:slf4j-simple:1.8.0-beta4")
    implementation("com.github.ajalt.clikt:clikt:3.2.0")
}

application {
    mainClass.set("spelling2g.AppKt")
}

tasks.withType<ShadowJar>() {
    manifest {
        attributes["Main-Class"] = "spelling2g.AppKt"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()

    testLogging {
        exceptionFormat = TestExceptionFormat.FULL
        events = mutableSetOf(TestLogEvent.FAILED, TestLogEvent.SKIPPED)

        showExceptions = true
        showCauses = true
        showStackTraces = false
        showStandardStreams = true
    }
}
