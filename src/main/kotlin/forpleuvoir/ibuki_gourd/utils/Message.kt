package forpleuvoir.ibuki_gourd.utils

import net.minecraft.client.MinecraftClient
import net.minecraft.network.MessageType
import net.minecraft.text.Text
import net.minecraft.util.Util


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
		mc.player?.sendChatMessage(message)
	}


	fun showChatMessage(message:Text){
		mc.inGameHud.addChatMessage(MessageType.CHAT,message, Util.NIL_UUID)
	}

	fun showInfo(message:Text){
		mc.inGameHud.addChatMessage(MessageType.GAME_INFO,message, Util.NIL_UUID)
	}

	fun showSystemMessage(message:Text){
		mc.inGameHud.addChatMessage(MessageType.SYSTEM,message, Util.NIL_UUID)
	}
}