package moe.forpleuvoir.ibukigourd.config.item

import moe.forpleuvoir.ibukigourd.input.KeyBind
import moe.forpleuvoir.nebula.config.ConfigValue
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment

@Environment(EnvType.CLIENT)
interface ConfigKeyBindValue : ConfigValue<KeyBind>