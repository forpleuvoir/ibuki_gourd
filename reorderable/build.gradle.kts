import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    // Kotlin JVM 与 Compose 编译器插件由 buildSrc 提供到 classpath（见 buildSrc/build.gradle.kts），此处不写版本
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.compose")
}

group = "moe.forpleuvoir.ibukigourd"

repositories {
    mavenCentral()
    mavenLocal()
    google()
}

dependencies {
    // compose-minecraft 的 jar 里自带它移植的 androidx.compose.animation / foundation / ui
    // （约 4100 个类），但 **不含 androidx.compose.runtime** —— @Composable 的编译产物必然引用
    // Composer / State / remember 这些类型，runtime 由 CMP 自己在 POM 里声明为必需依赖，
    // 无法回避（除非 CMP 把 runtime 也内嵌进它自己的 jar）。
    //
    // 除 runtime 与 ui-unit / ui-geometry / ui-util（CMP 的类签名要用）之外，
    // 其余传递依赖本项目一律不引入，保持依赖面最小。
    compileOnly(libs.composeMinecraft.common) {
        exclude(group = "org.jetbrains.androidx.lifecycle")
        exclude(group = "androidx.lifecycle")
        exclude(group = "org.jetbrains.androidx.savedstate")
        exclude(group = "androidx.savedstate")
        exclude(group = "androidx.navigationevent")
        exclude(group = "org.jetbrains.kotlinx", module = "atomicfu")
        exclude(group = "org.jetbrains.kotlinx", module = "atomicfu-jvm")
        exclude(group = "androidx.compose.runtime", module = "runtime-retain")
        exclude(group = "androidx.compose.runtime", module = "runtime-retain-desktop")
    }
    compileOnly(libs.bundles.kotlin)
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

// 拖拽排序库（Reorderable v3.0.0 源码内嵌，Apache-2.0，见 NOTICE.md）。
// 独立模块的原因：它对 Compose 的依赖是 compileOnly 级的（运行期由 compose-minecraft 满足），
// 与 aseprite 一样不需要 MC 环境即可参与编译。
tasks.withType<Jar>().configureEach {
    archiveBaseName = "ibukigourd-reorderable"
}
