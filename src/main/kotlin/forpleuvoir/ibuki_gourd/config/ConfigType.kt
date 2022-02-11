package forpleuvoir.ibuki_gourd.config


/**
 * 配置类型

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.config

 * 文件名 ConfigType

 * 创建时间 2021/12/9 17:33

 * @author forpleuvoir

 */
enum class ConfigType : IConfigType {
	BOOLEAN,
	INTEGER,
	DOUBLE,
	COLOR,
	STRING,
	STRING_LIST,
	OPTIONS,
	HOTKEY,
	Group,
	MAP,
	BOOLEAN_WITH_KEY_BIND
	;
}