package forpleuvoir.ibuki_gourd.config


/**
 * 配置修改时发出通知

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config

 * 文件名 IConfigNotifiable

 * 创建时间 2021/12/9 18:03

 * @author forpleuvoir

 */
interface IConfigNotifiable {
	/**
	 * 值有变动时调用
	 */
	fun onValueChange()

	/**
	 * 回调
	 * @param callback Function1<C, Unit>
	 */
	fun setOnValueChangedCallback(callback: (IConfigBase) -> Unit)

	/**
	 * 保存文件用
	 * @param callback Function1<IConfigBase, Unit>
	 */
	fun setOnValueChanged(callback: (IConfigBase) -> Unit)

}
