package forpleuvoir.ibuki_gourd.config

import forpleuvoir.ibuki_gourd.common.IJsonData
import net.minecraft.text.Text


/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config

 * 文件名 IConfigBase

 * 创建时间 2021/12/9 17:31

 * @author forpleuvoir

 */
interface IConfigBase : IJsonData {

	val type: ConfigType
	val name: String
	val remark: String
	val displayName: Text
	val displayRemark: Text


	fun isValueEquals(other: IConfigBase): Boolean

}