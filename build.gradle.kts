plugins {
    kotlin("jvm") version "2.1.0"
}

group = "ar.edu.itba.ss"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Clickt for CLI parsing
    implementation("com.github.ajalt.clikt:clikt:5.0.1")
    // Support for rendering markdown in help messages
    implementation("com.github.ajalt.clikt:clikt-markdown:5.0.1")

    // Logging
    implementation("org.slf4j:slf4j-simple:2.0.3")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.3")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}