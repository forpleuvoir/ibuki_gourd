package com.forpleuvoir.ibukigourd.mod.config

import com.forpleuvoir.ibukigourd.IbukiGourd
import com.forpleuvoir.ibukigourd.config.ClientModConfigManager
import com.forpleuvoir.ibukigourd.config.ModConfig
import com.forpleuvoir.ibukigourd.config.ServerModConfigManager
import com.forpleuvoir.ibukigourd.event.events.client.ClientLifecycleEvent
import com.forpleuvoir.ibukigourd.event.events.server.ServerSavingEvent
import com.forpleuvoir.nebula.config.impl.YamlConfigManagerSerializer
import com.forpleuvoir.nebula.config.item.impl.ConfigString
import com.forpleuvoir.nebula.event.EventSubscriber
import com.forpleuvoir.nebula.event.Subscriber

@ModConfig("config")
@EventSubscriber
object IbukiGourdServerConfig : ServerModConfigManager(IbukiGourd.MOD_ID, "config"), YamlConfigManagerSerializer {

	var SERVER_LANGUAGE by ConfigString("serve_language", "zh_cn")


	@Subscriber
	fun clientInit(event: ServerSavingEvent) {
		println("服务端自动保存：${SERVER_LANGUAGE}")
		SERVER_LANGUAGE = if (SERVER_LANGUAGE == "zh_cn") "en_us" else "zh_cn"
	}

}