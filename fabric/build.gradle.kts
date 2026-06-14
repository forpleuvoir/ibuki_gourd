@file:Suppress("UnstableApiUsage")

import java.util.*
import kotlin.random.Random

plugins {
    id("multiloader-loader")
    alias(libs.plugins.fabricLoom)
}

val modId: String = project.properties["mod_id"].toString()

repositories {
    maven {
        name = "Terraformers"
        url = uri("https://maven.terraformersmc.com/")
    }
}


fun ExternalModuleDependency.composeExclude() {
    exclude(group = "org.jetbrains.kotlin")
    exclude(group = "org.jetbrains.kotlinx")
    exclude(module = "annotations")
}

dependencies {
    minecraft(libs.minecraft)

    //Fabric
    implementation(libs.fabricLoader)
    implementation(libs.fabricApi)

    implementation(libs.fabricKotlin)
    implementation(libs.modMenu)

    //nebula
    implementation(libs.nebula)
    include(libs.nebula)

    includeInternal(implementation("org.jetbrains.compose.material3:material3:1.11.0-alpha07") {
        composeExclude()
    })
    includeInternal(implementation("org.jetbrains.compose.material3:material3-adaptive-navigation-suite:1.11.0-alpha07") {
        composeExclude()
    })
    includeInternal(implementation("com.materialkolor:material-kolor:${libs.versions.materialKolor.get()}") {
        composeExclude()
    })
    includeInternal(implementation(compose.desktop.currentOs) {
        exclude(module = "material-desktop")
        composeExclude()
    })
}

sourceSets {
    create("devOnly") {
        val test = project(":common").sourceSets["devOnly"]
        compileClasspath += main.get().compileClasspath + main.get().output + test.compileClasspath + test.output
        runtimeClasspath += main.get().runtimeClasspath + main.get().output + test.runtimeClasspath + test.output
    }
}

loom {
    val aw = project(":common").file("src/main/resources/${modId}.classtweaker")
    if (aw.exists()) {
        accessWidenerPath.set(aw)
    }

    runs {
        named("client") {
            client()
            configName = "Fabric Client"
            ideConfigGenerated(true)
            runDir("runs/client")
            val name: String = System.getenv("mcName") ?: "Dev${Random.nextInt(1000)}"
            val uuid: String = System.getenv("mcUUID") ?: UUID.randomUUID().toString()
            programArgs("--username", name, "--uuid", uuid)
            source(sourceSets["devOnly"])
        }
        named("server") {
            server()
            configName = "Fabric Server"
            ideConfigGenerated(true)
            runDir("runs/server")
        }
    }
}

val loaderAttribute = Attribute.of("io.github.mcgradleconventions.loader", String::class.java)
listOf<String>(
    "apiElements", "runtimeElements", "sourcesElements", "includeInternal", "modCompileClasspath"
).forEach {
    configurations.named(it) {
        attributes {
            attribute(loaderAttribute, "fabric")
        }
    }
}

sourceSets.configureEach {
    listOf(compileClasspathConfigurationName, runtimeClasspathConfigurationName).forEach {
        configurations.named(it) {
            attributes {
                attribute(loaderAttribute, "fabric")
            }
        }
    }
}

