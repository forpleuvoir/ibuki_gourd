package forpleuvoir.ibuki_gourd.mod

import forpleuvoir.ibuki_gourd.common.ModInfo
import forpleuvoir.ibuki_gourd.mod.initialize.IbukiGourdInitialize
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient


/**
 * 伊吹瓢Mod

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd

 * 文件名 IbukiGourdMod

 * 创建时间 2021/12/9 10:27

 * @author forpleuvoir

 */
@JvmField
val mc = MinecraftClient.getInstance()
@Environment(EnvType.CLIENT)
object IbukiGourdMod : ModInfo, ClientModInitializer {

	private val log = IbukiGourdLogger.getLogger(this::class.java)

	@JvmField
	val mc = MinecraftClient.getInstance()

	override val modId: String
		get() = "ibuki_gourd"

	override val modName: String
		get() = "Ibuki Gourd"


	/**
	 * Mod初始化
	 */
	override fun onInitializeClient() {
		IbukiGourdInitialize.initialize()
	}


}