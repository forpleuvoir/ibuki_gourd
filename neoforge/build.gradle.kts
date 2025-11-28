import org.gradle.internal.extensions.stdlib.capitalized
import java.util.*
import kotlin.random.Random

plugins {
    id("multiloader-loader")
    alias(libs.plugins.neoforgedModDev)
}

val modId: String = project.properties["mod_id"].toString()

neoForge {
    version = libs.versions.neoforge.get()
    // Automatically enable neoforge AccessTransformers if the file exists
    val at = project(":common").file("src/main/resources/META-INF/accesstransformer.cfg")
    if (at.exists()) {
        accessTransformers.from(at.absolutePath)
    }
    parchment {
        minecraftVersion = libs.versions.parchmentMinecraft
        mappingsVersion = libs.versions.parchment
    }
    runs {
        configureEach {
            systemProperty("neoforge.enabledGameTestNamespaces", modId)
            ideName = "NeoForge ${name.capitalized()} (${project.path})" // Unify the run config names with fabric
        }
        register("client") {
            client()
            val name: String = System.getenv("mcName") ?: "Dev${Random.nextInt(1000)}"
            val uuid: String = System.getenv("mcUUID") ?: UUID.randomUUID().toString()
            programArguments.addAll("--username", name, "--uuid", uuid)
        }
        register("data") {
            clientData()
            programArguments.addAll(
                "--mod",
                modId,
                "--all",
                "--output",
                file("src/generated/resources/").absolutePath,
                "--existing",
                file("src/main/resources/").absolutePath
            )
        }
        register("server") {
            server()
        }
    }
    mods {
        register(modId) {
            sourceSet(sourceSets.main.get())
        }
    }
}

sourceSets.main.get().resources { srcDir("src/generated/resources") }

val loaderAttribute = Attribute.of("io.github.mcgradleconventions.loader", String::class.java)

listOf("apiElements", "runtimeElements", "sourcesElements", "javadocElements").forEach {
    configurations.named(it) {
        attributes {
            attribute(loaderAttribute, "neoforge")
        }
    }
}

sourceSets.configureEach {
    listOf(compileClasspathConfigurationName, runtimeClasspathConfigurationName, getTaskName(null, "jarJar")).forEach {
        configurations.named(it) {
            attributes {
                attribute(loaderAttribute, "neoforge")
            }
        }
    }
}

dependencies {
    implementation(libs.forgeKotlin)
    jarJar("${libs.nebula.get()}:nebula")
}