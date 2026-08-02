plugins {
    id("multiloader-common")
    alias(libs.plugins.neoforgedModDev)
}

neoForge {
    neoFormVersion = libs.versions.neoForm.get()
    val at = file("src/main/resources/META-INF/accesstransformer.cfg")
    if (at.exists()) {
        accessTransformers.from(at.absolutePath)
    }
}

dependencies {
    compileOnly(libs.bundles.kotlin)

    compileOnly(libs.mixin)
    compileOnly(libs.mixinExtras.common)
    annotationProcessor(libs.mixinExtras.common)

    api(libs.nebula)

    api("org.jetbrains.compose.material3:material3:${libs.versions.material3.get()}")
    api("org.jetbrains.compose.material3:material3-adaptive-navigation-suite:${libs.versions.material3.get()}")
    api(libs.materialKolor)
    api(libs.backdrop)
    api(libs.reorderable)
    api(compose.desktop.currentOs) {
        exclude(module = "material-desktop")
    }

    //IMBlocker 兼容
    compileOnly("maven.modrinth:WMDesFsZ:MQ96z3kk")

    testImplementation(kotlin("test-junit5"))
}

tasks.test {
    useJUnitPlatform()
}

sourceSets {
    create("devOnly") {
        compileClasspath += main.get().compileClasspath + main.get().output
    }
}

configurations {
    create("commonJava") {
        isCanBeResolved = false
        isCanBeConsumed = true
    }
    create("commonKotlin") {
        isCanBeResolved = false
        isCanBeConsumed = true
    }
    create("commonResources") {
        isCanBeResolved = false
        isCanBeConsumed = true
    }
}

artifacts {
    add("commonJava", sourceSets.main.get().java.sourceDirectories.singleFile)
    add("commonKotlin", sourceSets.main.get().kotlin.sourceDirectories.filter { !it.name.endsWith("java") }.singleFile)
    add("commonResources", file("src/main/resources"))
}

val loaderAttribute = Attribute.of("io.github.mcgradleconventions.loader", String::class.java)
listOf<String>(
    "apiElements", "runtimeElements", "sourcesElements"
).forEach {
    configurations.named(it) {
        attributes {
            attribute(loaderAttribute, "common")
        }
    }
}

sourceSets.configureEach {
    listOf(compileClasspathConfigurationName, runtimeClasspathConfigurationName).forEach {
        configurations.named(it) {
            attributes {
                attribute(loaderAttribute, "common")
            }
        }
    }
}
