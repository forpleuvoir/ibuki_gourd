plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.compose")
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation("org.jetbrains.compose.runtime:runtime-desktop:1.11.0")
    implementation("org.jetbrains.compose.runtime:runtime-saveable-desktop:1.11.0")
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")
}

tasks.test {
    useJUnitPlatform()
    failOnNoDiscoveredTests.set(false)
}
