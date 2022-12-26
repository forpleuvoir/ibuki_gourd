package com.forpleuvoir.ibukigourd.event.events

import com.forpleuvoir.nebula.event.Event
import net.fabricmc.loader.api.metadata.ModMetadata

/**
 * 模组初始化事件
 * @property meta ModMetadata
 * @constructor
 */
class ModInitializerEvent(@JvmField val meta: ModMetadata) : Event