package forpleuvoir.ibuki_gourd.common

import net.fabricmc.loader.api.FabricLoader


/**
 * 模组信息

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.mod

 * 文件名 ModInfo

 * 创建时间 2021/12/9 9:50

 * @author forpleuvoir

 */
interface ModInfo {

	/**
	 * Mod ID
	 * @return String Mod ID
	 */
	val modId: String

	/**
	 * Mod 名称
	 * @return String Mod 名称
	 */
	val modName: String


	/**
	 * 获取Mod版本
	 * @return String Mod版本
	 */
	val version: String
		get() = FabricLoader.getInstance().getModContainer(modId).get().metadata.version.friendlyString

}