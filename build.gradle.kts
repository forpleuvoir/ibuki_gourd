import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

plugins {
    java
    signing
    alias(libs.plugins.fabricLoom)
    alias(libs.plugins.kotlinJVM)
    id("maven-publish")
}

repositories {
    mavenCentral()
    mavenLocal()
    maven { url = uri("https://www.jitpack.io") }
    maven { url = uri("https://maven.terraformersmc.com/") }
//	maven { url = uri("https://maven.forpleuvoir.moe/releases") }
    maven { url = uri("https://maven.forpleuvoir.moe/snapshots") }

    maven {
        name = "IzzelAliz Maven"
        url = uri("https://maven.izzel.io/releases/")
    }
}

val time: String get() = SimpleDateFormat("yyyyMMdd").format(Date())

val gitHash: String by lazy {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine("git", "rev-parse", "--short", "HEAD") // 获取短哈希值
        standardOutput = stdout
    }
    stdout.toString().trim()
}


val modName: String = properties["archives_base_name"].toString()
version = libs.versions.modVersion.get()
group = properties["maven_group"].toString()
val versionWithGitHash: String = "$version+$gitHash"

dependencies {
    minecraft(libs.minecraft)
    mappings("${libs.yarnMappings.get()}:v2")
    modImplementation(libs.fabricLoader)
    modImplementation(libs.fabricApi)

    modImplementation(libs.fabricKotlin)

    //其他mod依赖
    modImplementation(libs.modMenu)

    //兼容测试
    modCompileOnly("icyllis.modernui", "ModernUI-Core", "3.11.1")
    modCompileOnly("icyllis.modernui", "ModernUI-Markdown", "3.11.1")
    modCompileOnly("icyllis.modernui", "ModernUI-Fabric", "1.21.4-3.11.1.11")
    //nebula
    include("${libs.nebula.get()}:nebula")
    api(libs.nebula)

    //其他第三方库依赖

    //test
    testImplementation(kotlin("test"))
}

loom {
    splitEnvironmentSourceSets()
    mods {
        create(modName) {
            sourceSet(sourceSets.main.get())
            sourceSet(sourceSets["client"])
        }
    }
    accessWidenerPath.set(file("src/main/resources/ibukigourd.accesswidener"))
}

sourceSets {
    val devClient = create("devClientTest") {
        compileClasspath += main.get().compileClasspath + main.get().output + sourceSets["client"].compileClasspath + sourceSets["client"].output
        runtimeClasspath += main.get().runtimeClasspath + main.get().output + sourceSets["client"].runtimeClasspath + sourceSets["client"].output
    }
    val devSever = create("devServerTest") {
        compileClasspath += main.get().compileClasspath + main.get().output
        runtimeClasspath += main.get().runtimeClasspath + main.get().output
    }
    named("test") {
        compileClasspath += devClient.compileClasspath + devClient.output
        runtimeClasspath += devClient.runtimeClasspath + devClient.output
    }
}

loom {
    runs {
        create("clientTest") {
            val name: String = System.getenv("mcName") ?: "Dev${Random.nextInt(1000)}"
            val uuid: String = System.getenv("mcUUID") ?: UUID.randomUUID().toString()
            programArgs("--username", name, "--uuid", uuid)
            client()
            name("ClientTest")
            ideConfigGenerated(true)
            source(sourceSets["devClientTest"])
        }
        create("serverTest") {
            server()
            name("ServerTest")
            runDir("server_run")
            ideConfigGenerated(true)
            source(sourceSets["devServerTest"])
        }
    }
}

java {
    withSourcesJar()
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        apiVersion.set(KotlinVersion.KOTLIN_2_0)
        freeCompilerArgs.add("-Xjvm-default=all")
    }
}



tasks {

    processResources {
        inputs.property("version", version)
        filteringCharset = "UTF-8"
        filesMatching("fabric.mod.json") {
            expand("version" to version)
        }
    }

    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(21)
        targetCompatibility = JavaVersion.VERSION_21.toString()
        sourceCompatibility = JavaVersion.VERSION_21.toString()
    }

    named<JavaCompile>("compileClientJava") {
        dependsOn("compileJava")
    }
    named<JavaCompile>("compileDevClientTestJava") {
        dependsOn("compileClientJava")
    }
    named<JavaCompile>("compileDevServerTestJava") {
        dependsOn("compileJava")
    }

    named<KotlinCompile>("compileClientKotlin") {
        dependsOn("compileKotlin")
    }
    named<KotlinCompile>("compileDevClientTestKotlin") {
        dependsOn("compileClientKotlin")
    }
    named<KotlinCompile>("compileDevServerTestKotlin") {
        dependsOn("compileKotlin")
    }

    jar {
        from("LICENSE") {
            rename { "${it}_$modName" }
        }
    }

    register<Copy>("modJar") {
        dependsOn(remapJar)
        mustRunAfter(remapJar)
        val outPath = "$rootDir/modJar/$version"
        val name = remapJar.get().archiveFileName.get()
        val newName = "$modName-$versionWithGitHash.$time-minecraft.${libs.versions.minecraftVersion.get()}-fabric.jar"
        from("build/libs")
        into(outPath)
        include(name)
        doLast {
            delete("$outPath/$newName")
            file("$outPath/$name").renameTo(file("$outPath/$newName"))
        }
    }

}

publishing {
    //https://reposilite.com/guide/gradle
    repositories {
        maven {
            name = "releases"
            url = uri("https://maven.forpleuvoir.moe/releases")
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
        maven {
            name = "snapshots"
            url = uri("https://maven.forpleuvoir.moe/snapshots")
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>(project.name) {
            artifact(tasks.remapJar)
            artifact(tasks.remapSourcesJar)
            pom {
                name.set(project.name)
                description.set("forpleuvoir的Minecraft基础前置mod")
                url.set("https://github.com/forpleuvoir/ibuki_gourd")
                licenses {
                    license {
                        name.set("GNU General Public License, version 3 (GPLv3)")
                        url.set("https://www.gnu.org/licenses/gpl-3.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("forpleuvoir")
                        name.set("forpleuvoir")
                        email.set("forpleuvoir@gmail.com")
                    }
                }
            }
        }
    }
}