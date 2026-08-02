import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.type.ArtifactTypeDefinition
import org.gradle.internal.extensions.stdlib.capitalized
import java.util.UUID
import kotlin.random.Random

plugins {
    id("multiloader-loader")
    alias(libs.plugins.neoforgedModDev)
}

val modId: String = project.findProperty("mod_id").toString()

/*
 * 需要同时：
 *
 * 1. 暴露到 api。
 * 2. 连同传递依赖一起打入 JarJar。
 *
 * 的依赖统一声明在这里。
 */
val bundledApi = configurations.create("bundledApi") {
    isCanBeResolved = false
    isCanBeConsumed = false
}

/*
 * 专门解析 bundledApi 的完整传递依赖树。
 *
 * NeoForge 的 jarJar 默认只嵌入直接依赖，因此不能简单地让
 * jarJar extendsFrom bundledApi。
 */
val jarJarInternal = configurations.create("jarJarInternal") {
    isCanBeResolved = true
    isCanBeConsumed = false

    extendsFrom(bundledApi)

    attributes {
        attribute(
            Usage.USAGE_ATTRIBUTE,
            project.objects.named(Usage.JAVA_RUNTIME)
        )
        attribute(
            LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE,
            project.objects.named(LibraryElements.JAR)
        )
        attribute(
            Category.CATEGORY_ATTRIBUTE,
            project.objects.named(Category.LIBRARY)
        )
        attribute(
            Bundling.BUNDLING_ATTRIBUTE,
            project.objects.named(Bundling.EXTERNAL)
        )
    }
}

/*
 * bundledApi 中声明的依赖同时对当前模块的 api 可见。
 *
 * 这样就不需要再写：
 *
 * jarJarInternal(api(...))
 */
configurations.named("api") {
    extendsFrom(bundledApi)
}

fun ExternalModuleDependency.composeExclude() {
    exclude(group = "org.jetbrains.kotlin")
    exclude(group = "org.jetbrains.kotlinx")
    exclude(module = "annotations")
}

/*
 * Compose Multiplatform 最近迁移过部分依赖坐标。
 *
 * org.jetbrains.* 下的旧模块通常只是几 KB 的重定向空壳，
 * 不能和实际的 androidx.* JAR 一起交给 NeoForge JarJar。
 */
val composeModuleReplacements = mapOf(
    "org.jetbrains.compose.runtime:runtime-desktop" to
            "androidx.compose.runtime:runtime-desktop",

    "org.jetbrains.compose.runtime:runtime-saveable-desktop" to
            "androidx.compose.runtime:runtime-saveable-desktop",

    "org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose-desktop" to
            "androidx.lifecycle:lifecycle-runtime-compose-desktop",

    "org.jetbrains.androidx.savedstate:savedstate-compose-desktop" to
            "androidx.savedstate:savedstate-compose-desktop"
)

dependencies {
    /*
     * 当新旧坐标同时出现在依赖树中时，选择实际的 androidx 实现，
     * 不再保留旧的重定向模块。
     */
    modules {
        composeModuleReplacements.forEach { (oldModule, replacementModule) ->
            module(oldModule) {
                replacedBy(
                    replacementModule,
                    "Compose Multiplatform artifact relocation"
                )
            }
        }
    }

    implementation(libs.forgeKotlin)

    /*
     * Nebula 只嵌入自身。
     *
     * 如果以后需要连 Nebula 的传递依赖一起嵌入，
     * 可以将它改为 bundledApi(libs.nebula)。
     */
    api(libs.nebula)
    jarJar(libs.nebula)

    /*
     * 所有需要：
     *
     * 1. 暴露给主源码；
     * 2. 展开全部传递依赖；
     * 3. 嵌入最终 NeoForge JAR；
     *
     * 的依赖都声明到 bundledApi。
     */
    bundledApi(
        "org.jetbrains.compose.material3:" +
                "material3:${libs.versions.material3.get()}"
    ) {
        composeExclude()
    }

    bundledApi(
        "org.jetbrains.compose.material3:" +
                "material3-adaptive-navigation-suite:" +
                libs.versions.material3.get()
    ) {
        composeExclude()
    }

    /*
     * Material Kolor 需要 atomicfu，但 Kotlin for Forge 没有提供。
     *
     * 单独作为正常 Maven 模块声明，不能再把 api(...) 返回的
     * Dependency 对象直接传给 jarJar(...)。
     */
    bundledApi("org.jetbrains.kotlinx:atomicfu:0.28.0")

    bundledApi(
        "com.materialkolor:" +
                "material-kolor:${libs.versions.materialKolor.get()}"
    ) {
        composeExclude()
    }

    bundledApi(
        "io.github.kyant0:" +
                "backdrop:${libs.versions.backdrop.get()}"
    ) {
        composeExclude()
    }

    bundledApi(
        "sh.calvin.reorderable:" +
                "reorderable:${libs.versions.reorderable.get()}"
    ) {
        composeExclude()
    }

    bundledApi(compose.desktop.currentOs) {
        /*
         * currentOs 依赖树中可能存在 material-desktop，
         * 但项目使用的是 material3。
         */
        exclude(module = "material-desktop")
        composeExclude()
    }

    // IMBlocker 兼容
    compileOnly("maven.modrinth:WMDesFsZ:MQ96z3kk")
}

/*
 * 模拟 Loom includeInternal：
 *
 * 1. 解析 jarJarInternal 的完整传递依赖树。
 * 2. 将每个解析完成的 Maven 模块提升为 jarJar 直接依赖。
 * 3. 将这些依赖设为非传递，避免 jarJar 再解析一次依赖树。
 *
 * 不能使用 resolvedConfiguration.lenientConfiguration：
 *
 * - 它是旧 API；
 * - 会丢失解析后的 variant 信息；
 * - lenient 模式可能静默忽略打包依赖错误。
 */
configurations.named("jarJar") {
    dependencies.addAllLater(
        project.provider {
            val resolvedCoordinates = jarJarInternal.incoming.artifactView {
                attributes {
                    attribute(
                        ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE,
                        ArtifactTypeDefinition.JAR_TYPE
                    )
                }
            }.artifacts.resolvedArtifacts.get()
                .map { artifact ->
                    val component = artifact.id.componentIdentifier

                    val id = component as? ModuleComponentIdentifier
                        ?: error(
                            "jarJarInternal 只支持外部 Maven 模块，" +
                                    "但发现了非模块依赖：${artifact.file.absolutePath}"
                        )

                    Triple(
                        id.group,
                        id.module,
                        id.version
                    )
                }
                .distinct()

            /*
             * 额外保护：
             *
             * 即使某个 Gradle/Compose 版本没有正确应用 replacedBy，
             * 也不允许把旧重定向 JAR 放入最终产物。
             */
            val resolvedModules = resolvedCoordinates
                .mapTo(hashSetOf()) { (group, module, _) ->
                    "$group:$module"
                }

            composeModuleReplacements.forEach { (oldModule, replacementModule) ->
                check(
                    oldModule !in resolvedModules ||
                            replacementModule in resolvedModules
                ) {
                    buildString {
                        append("发现 Compose 重定向模块 ")
                        append(oldModule)
                        append("，但没有解析到对应实现 ")
                        append(replacementModule)
                    }
                }
            }

            resolvedCoordinates
                .filterNot { (group, module, _) ->
                    "$group:$module" in composeModuleReplacements
                }
                .sortedBy { (group, module, version) ->
                    "$group:$module:$version"
                }
                .map { (group, module, version) ->
                    (
                            project.dependencies.create(
                                "$group:$module:$version"
                            ) as ExternalModuleDependency
                            ).apply {
                            /*
                             * 这份依赖列表已经是展开完成后的结果。
                             *
                             * 必须关闭传递解析，否则 jarJar 会重新解析，
                             * 重新引入重定向模块或改变已选中的版本。
                             */
                            isTransitive = false

                            because(
                                "Resolved and flattened from jarJarInternal"
                            )
                        }
                }
        }
    )
}

sourceSets {
    create("devOnly") {
        val commonDevOnly = project(":common").sourceSets["devOnly"]

        compileClasspath +=
            main.get().compileClasspath +
                    main.get().output +
                    commonDevOnly.compileClasspath +
                    commonDevOnly.output

        runtimeClasspath +=
            main.get().runtimeClasspath +
                    main.get().output +
                    commonDevOnly.runtimeClasspath +
                    commonDevOnly.output
    }
}

neoForge {
    version = libs.versions.neoforge.get()

    val accessTransformer = project(":common")
        .file("src/main/resources/META-INF/accesstransformer.cfg")

    if (accessTransformer.exists()) {
        accessTransformers.from(accessTransformer)
    }

    runs {
        configureEach {
            systemProperty(
                "neoforge.enabledGameTestNamespaces",
                modId
            )

            ideName =
                "NeoForge ${name.capitalized()} (${project().path})"
        }

        register("client") {
            client()

            val playerName =
                System.getenv("mcName")
                    ?: "Dev${Random.nextInt(1000)}"

            val playerUuid =
                System.getenv("mcUUID")
                    ?: UUID.randomUUID().toString()

            programArguments.addAll(
                "--username",
                playerName,
                "--uuid",
                playerUuid
            )

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

sourceSets.main.get().resources {
    srcDir("src/generated/resources")
}

val loaderAttribute = Attribute.of(
    "io.github.mcgradleconventions.loader",
    String::class.java
)

listOf(
    "apiElements",
    "runtimeElements",
    "sourcesElements"
).forEach { configurationName ->
    configurations.named(configurationName) {
        attributes {
            attribute(loaderAttribute, "neoforge")
        }
    }
}

sourceSets.configureEach {
    listOf(
        compileClasspathConfigurationName,
        runtimeClasspathConfigurationName,
        getTaskName(null, "jarJar")
    ).forEach { configurationName ->
        configurations.named(configurationName) {
            attributes {
                attribute(loaderAttribute, "neoforge")
            }
        }
    }
}