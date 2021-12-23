package forpleuvoir.ibuki_gourd.utils

import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.utils

 * 文件名 TextUtil

 * 创建时间 2021/12/23 20:59

 * @author forpleuvoir

 */

val Text.width: Int
	get() = MinecraftClient.getInstance().textRenderer.getWidth(this)