import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    // Kotlin JVM 插件由 buildSrc 提供到 classpath（见 buildSrc/build.gradle.kts），此处不写版本
    id("org.jetbrains.kotlin.jvm")
}

group = "moe.forpleuvoir.ibukigourd"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test-junit5"))
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_25)
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

// ASE 解析库：.ase 文件格式解析 / 元数据读取 / 像素解码，零第三方依赖。
// 由 common（运行时）与测试共同使用；独立模块可在无 MC 环境下直接验证。
tasks.withType<Jar>().configureEach {
    archiveBaseName = "ibukigourd-aseprite"
}

tasks.test {
    useJUnitPlatform()
}
