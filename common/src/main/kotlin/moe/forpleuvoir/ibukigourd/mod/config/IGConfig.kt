package moe.forpleuvoir.ibukigourd.mod.config

import moe.forpleuvoir.ibukigourd.IbukiGourd
import moe.forpleuvoir.ibukigourd.config.ClientModConfigManager

/**
 * IbukiGourd 自身配置。
 *
 * 原 Compose Desktop 离屏渲染 UI 相关的配置项（Gui / 主题 / Toast / 图集缓存 / 打开界面快捷键）
 * 已随 UI 迁移至 compose-minecraft 一并移除，待新 UI 落地后在此重建。
 */
object IGConfig : ClientModConfigManager(IbukiGourd.MOD_ID, "config")