import org.gradle.internal.extensions.stdlib.capitalized
import java.util.*
import kotlin.random.Random

plugins {
    id("multiloader-loader")
    alias(libs.plugins.neoforgedModDev)
}

val modId: String = project.findProperty("mod_id").toString()

val jarJarInternal = configurations.create("jarJarInternal") {
    isCanBeResolved = true
    isCanBeConsumed = false
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage.JAVA_RUNTIME))
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.objects.named(LibraryElements.JAR))
        attribute(Category.CATEGORY_ATTRIBUTE, project.objects.named(Category.LIBRARY))
        attribute(Bundling.BUNDLING_ATTRIBUTE, project.objects.named(Bundling.EXTERNAL))
    }
}

fun ExternalModuleDependency.composeExclude() {
    exclude(group = "org.jetbrains.kotlin")
    exclude(group = "org.jetbrains.kotlinx")
    exclude(module = "annotations")
}

dependencies {
    implementation(libs.forgeKotlin)

    api(libs.nebula)?.let { jarJar(it) }

    jarJarInternal(api("org.jetbrains.compose.material3:material3:${libs.versions.material3.get()}") {
        composeExclude()
    })
    jarJarInternal(api("org.jetbrains.compose.material3:material3-adaptive-navigation-suite:${libs.versions.material3.get()}") {
        composeExclude()
    })
    //material-kolor 依赖但是 kotlinforege没有的
    api("org.jetbrains.kotlinx:atomicfu:0.28.0")?.let { jarJar(it) }

    jarJarInternal(api("com.materialkolor:material-kolor:${libs.versions.materialKolor.get()}") {
        composeExclude()
    })
    jarJarInternal(api("io.github.kyant0:backdrop:${libs.versions.backdrop.get()}") {
        composeExclude()
    })
    jarJarInternal(api(compose.desktop.currentOs) {
        exclude(module = "material-desktop")
        composeExclude()
    })

}

// 解析 compose 传递树，以 Maven 依赖注入到 jarJar（jarJar task 会处理 module name 校验以外的内容）
configurations.named("jarJar") {
    dependencies.addAllLater(project.provider {
        val resolved = configurations.named("jarJarInternal").get().resolvedConfiguration.lenientConfiguration.artifacts
        resolved.map { artifact ->
            val id = artifact.moduleVersion.id
            project.dependencies.create("${id.group}:${id.name}:${id.version}")
        }
    })
}

sourceSets {
    create("devOnly") {
        val test = project(":common").sourceSets["devOnly"]
        compileClasspath += main.get().compileClasspath + main.get().output + test.compileClasspath + test.output
        runtimeClasspath += main.get().runtimeClasspath + main.get().output + test.runtimeClasspath + test.output
    }
}

neoForge {
    version = libs.versions.neoforge.get()
    val at = project(":common").file("src/main/resources/META-INF/accesstransformer.cfg")
    if (at.exists()) {
        accessTransformers.from(at)
    }
    runs {
        configureEach {
            systemProperty("neoforge.enabledGameTestNamespaces", modId)
            ideName = "NeoForge ${name.capitalized()} (${project().path})"
        }
        register("client") {
            client()
            val name: String = System.getenv("mcName") ?: "Dev${Random.nextInt(1000)}"
            val uuid: String = System.getenv("mcUUID") ?: UUID.randomUUID().toString()
            programArguments.addAll("--username", name, "--uuid", uuid)
            gameDirectory = file("runs/client")
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
            gameDirectory = file("runs/server")
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

listOf("apiElements", "runtimeElements", "sourcesElements").forEach {
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
