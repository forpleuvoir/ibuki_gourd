package forpleuvoir.ibuki_gourd.utils

import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text


/**
 * 消息工具类

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.utils

 * 文件名 Message

 * 创建时间 2021/12/9 17:05

 * @author forpleuvoir

 */
object Message {
	private val mc = MinecraftClient.getInstance()

	/**
	 * 发送消息
	 * @param message String
	 */
	fun sendChatMessage(message: String) {
		if (message.startsWith("/"))
			mc.player?.networkHandler?.sendCommand(message)
		else
			mc.player?.networkHandler?.sendChatMessage(message)
	}

	fun showChatMessage(message: Text) {
		mc.inGameHud.chatHud.addMessage(message)
	}

	fun showInfo(message: Text) {
		mc.inGameHud.setOverlayMessage(message, false)
	}

}