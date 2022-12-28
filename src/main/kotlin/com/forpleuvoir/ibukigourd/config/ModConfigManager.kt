package com.forpleuvoir.ibukigourd.config

import com.forpleuvoir.nebula.config.impl.LocalConfigManager

abstract class ModConfigManager(protected val modID: String, key: String) : LocalConfigManager(key) {
}