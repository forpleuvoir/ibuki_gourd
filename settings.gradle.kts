pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        //Fabric
        exclusiveContent {
            forRepository {
                maven {
                    name = "Fabric"
                    url = uri("https://maven.fabricmc.net/")
                }
            }
            filter {
                includeGroupByRegex("net\\.fabricmc.*")
            }
        }
        maven { url = uri("https://maven.fabricmc.net/") }
        maven { url = uri("https://www.jitpack.io") }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention").version("1.0.0")
}


rootProject.name = "IbukiGourd"

include("common")
include("fabric")
include("neoforge")
include("compose-test")