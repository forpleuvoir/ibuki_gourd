package com.forpleuvoir.ibukigourd.config.item

import com.forpleuvoir.ibukigourd.input.KeyBind
import com.forpleuvoir.nebula.config.ConfigValue
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment

@Environment(EnvType.CLIENT)
interface ConfigKeyBindValue : ConfigValue<KeyBind>