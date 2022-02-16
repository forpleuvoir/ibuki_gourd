package forpleuvoir.ibuki_gourd.javascript

import org.mozilla.javascript.Context
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

/**
 *

 * 项目名 ibuki_gourd

 * 包名 forpleuvoir.ibuki_gourd.javascript

 * 文件名 JavaScriptUtil

 * 创建时间 2022/2/14 15:10

 * @author forpleuvoir

 */

val scriptEngin: ScriptEngine get() = ScriptEngineManager().getEngineByName("Rhino")

val context: Context get() = Context.getCurrentContext()