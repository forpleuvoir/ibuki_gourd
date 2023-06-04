package moe.forpleuvoir.ibukigourd.config

import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS

@Retention(RUNTIME)
@Target(CLASS)
annotation class ModConfig(val name: String)
