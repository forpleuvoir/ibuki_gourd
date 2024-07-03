package moe.forpleuvoir.ibukigourd.mod.config

import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.config.ModConfig
import moe.forpleuvoir.ibukigourd.config.ServerModConfigManager
import moe.forpleuvoir.nebula.config.item.impl.string

@ModConfig("config")
object IbukiGourdServerConfig : ServerModConfigManager(IbukiGourd.metadata, "config") {

    var SERVER_LANGUAGE by string("serve_language", "zh_cn")


}