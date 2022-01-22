package forpleuvoir.ibuki_gourd.mod.utils

import forpleuvoir.ibuki_gourd.common.ModLang
import forpleuvoir.ibuki_gourd.mod.IbukiGourdMod


/**
 * 语言

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.mod.utils

 * 文件名 IbukiGourdLang

 * 创建时间 2021/12/9 16:20

 * @author forpleuvoir

 */
enum class IbukiGourdLang(override val key:String): ModLang {
	Initializing("initializing"),
	Initialized("initialized"),
	Red("color.red"),
	Green("color.green"),
	Blue("color.blue"),
	Alpha("color.alpha"),
	On("on"),
	Off("off"),
	Confirm("confirm"),
	Cancel("cancel"),
	Post("post"),
	Add("add"),
	Delete("delete"),
	Update("update"),
	Apply("apply"),
	Toggle("toggle"),
	Value("value"),
	Hotkey("hotkey"),
	Execute("execute"),
	Edit("edit"),
	Enable("enable"),
	Enabled("enabled"),
	Disable("disable"),
	Disabled("disabled"),
	Status("status"),
	Rest("rest"),
	Save("save"),
	KeyEnvironment("key_environment"),
	SetFromJsonFailed("config.set_from_json_failed"),
	;

	override val modId: String
		get() = IbukiGourdMod.modId
}