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

dependencies {
    minecraft(libs.minecraft)

    //Fabric
    implementation(libs.fabricLoader)
    implementation(libs.fabricApi)

    implementation(libs.fabricKotlin)
    implementation(libs.modMenu)

    //nebula
    api(libs.nebula)
    include(libs.nebula)
}

sourceSets {
    create("devClientTest") {
        val test = project(":common").sourceSets["devClientTest"]
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
            source(sourceSets["devClientTest"])
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

