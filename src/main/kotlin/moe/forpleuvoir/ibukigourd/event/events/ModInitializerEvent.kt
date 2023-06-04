package moe.forpleuvoir.ibukigourd.event.events

import moe.forpleuvoir.nebula.event.Event
import net.fabricmc.loader.api.metadata.ModMetadata

/**
 * 模组初始化事件
 * @property meta ModMetadata
 * @constructor
 */
class ModInitializerEvent(@JvmField val meta: ModMetadata) : Event