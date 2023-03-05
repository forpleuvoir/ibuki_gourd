import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.archivesName
import java.text.SimpleDateFormat
import java.util.*

plugins {
	java
	signing
	id("fabric-loom") version "0.12-SNAPSHOT"
	kotlin("jvm") version "1.7.22"
	id("maven-publish")
}

repositories {
	mavenCentral()
	mavenLocal()
	maven { url = uri("https://www.jitpack.io") }
	maven { url = uri("https://maven.terraformersmc.com/") }
	maven { url = uri("https://maven.forpleuvoir.com:11443/releases") }
	maven { url = uri("https://maven.forpleuvoir.com:11443/snapshots") }
}

val time: String = SimpleDateFormat("yyyyMMdd").format(Date())
version = "${properties["mod_version"]}.$time"
group = properties["maven_group"].toString()

val minecraftVersion: String = properties["minecraft_version"].toString()
val yarnMappings: String = properties["yarn_mappings"].toString()
val fabricLoaderVersion: String = properties["fabric_loader_version"].toString()
val fabricApiVersion: String = properties["fabric_api_version"].toString()
val fabricKotlinVersion: String = properties["fabric_kotlin_version"].toString()
val modMenuVersion: String = properties["mod_menu_version"].toString()

val nebulaVersion: String = properties["nebula_version"].toString()

val snakeyaml = "2.0"
val forKomaVersion = "1.2.0"
val hoconVersion = "1.4.2"
val konbini = "0.1.3"

loom {
	accessWidenerPath.set(file("src/main/resources/ibukigourd.accesswidener"))
}

dependencies {
	minecraft("com.mojang:minecraft:$minecraftVersion")
	mappings("net.fabricmc:yarn:$yarnMappings:v2")
	modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
	modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")

	//其他mod依赖
	modImplementation("net.fabricmc:fabric-language-kotlin:$fabricKotlinVersion")
	modImplementation("com.terraformersmc:modmenu:$modMenuVersion")

	//其他第三方库依赖
	implementation("com.forpleuvoir.nebula:common:$nebulaVersion")
	include("com.forpleuvoir.nebula:common:$nebulaVersion")
	implementation("com.forpleuvoir.nebula:config:$nebulaVersion")
	include("com.forpleuvoir.nebula:config:$nebulaVersion")
	implementation("com.forpleuvoir.nebula:event:$nebulaVersion")
	include("com.forpleuvoir.nebula:event:$nebulaVersion")
	implementation("com.forpleuvoir.nebula:serialization:$nebulaVersion")
	include("com.forpleuvoir.nebula:serialization:$nebulaVersion")
	include("org.yaml:snakeyaml:$snakeyaml")
	include("cc.ekblad:4koma:$forKomaVersion")
	include("cc.ekblad.konbini:konbini:$konbini")
	include("com.typesafe:config:$hoconVersion")
}


tasks.apply {

	processResources {
		inputs.property("version", version)
		filteringCharset = "UTF-8"
		filesMatching("fabric.mod.json") {
			expand("version" to version)
		}
	}

	withType(JavaCompile::class.java).configureEach {
		options.encoding = "UTF-8"
		options.release.set(17)
	}

	jar {
		from("LICENSE") {
			rename { "${it}_${project.archivesName}" }
		}
	}

}


java {
	withSourcesJar()
	toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}



publishing {
	//https://reposilite.com/guide/gradle
	repositories {
		maven {
			name = "releases"
			url = uri("https://maven.forpleuvoir.com/releases")
			credentials(PasswordCredentials::class)
			authentication {
				create<BasicAuthentication>("basic")
			}
		}
		maven {
			name = "snapshots"
			url = uri("https://maven.forpleuvoir.com/snapshots")
			credentials(PasswordCredentials::class)
			authentication {
				create<BasicAuthentication>("basic")
			}
		}
	}
	publications {
		create<MavenPublication>(project.name) {
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
			artifact("remapJar") {
				builtBy(tasks.remapJar)
			}
			artifact("sourcesJar") {
				builtBy(tasks.remapSourcesJar)
			}
		}
	}
}
