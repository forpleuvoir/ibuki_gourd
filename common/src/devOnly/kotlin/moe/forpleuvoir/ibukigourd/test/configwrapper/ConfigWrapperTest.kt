package moe.forpleuvoir.ibukigourd.test.configwrapper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.mod.config.IGConfig
import moe.forpleuvoir.ibukigourd.test.TestConfig
import moe.forpleuvoir.ibukigourd.ui.configwrapper.ConfigManagerWrapper
import moe.forpleuvoir.ibukigourd.test.TestScreen
import moe.forpleuvoir.ibukigourd.ui.configwrapper.ConfigRowWrapper
import moe.forpleuvoir.ibukigourd.ui.configwrapper.ConfigsWrapper
import moe.forpleuvoir.ibukigourd.ui.configwrapper.LocalSearchFilter
import moe.forpleuvoir.ibukigourd.ui.configwrapper.UIWrappers
import moe.forpleuvoir.ibukigourd.ui.configwrapper.asState
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Button
import moe.forpleuvoir.ibukigourd.ui.sokitsu.IntField
import moe.forpleuvoir.ibukigourd.ui.sokitsu.IntSlider
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Surface
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Switch
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.nebula.config.ConfigGroup
import moe.forpleuvoir.nebula.config.ConfigManager
import moe.forpleuvoir.nebula.config.ConfigNode
import moe.forpleuvoir.nebula.config.item.ConfigRange

/**
 * 配置 GUI 包装器框架测试屏（chunk 1：框架层）。
 *
 * 页面上是 devOnly 的 [TestConfig] 真身（含嵌套分组与各种类型），验证点：
 * 1. 注册表分发：bool / int / long / float / double / string / enum / duration / color / vector /
 *    keycode / keybind 都有真控件；list / map / pairList 尚未注册，应落到兜底行「暂不支持」而不是整行消失；
 * 2. 行骨架：悬停底色、注释单行省略 + 截断后悬停气泡、重置按钮（处于默认值时禁用）、整行点击；
 * 3. 分组：默认展开阈值、折叠展开（右侧箭头 180° 翻转）、层级缩进与分割线；
 * 4. 搜索过滤：[LocalSearchFilter] 提供集合时，只保留集合内的子项（分组标题行保留）。
 */
fun ConfigWrapperTestScreen() = TestScreen {
    Surface(Modifier.fillMaxSize()) {
        var filtering by remember { mutableStateOf(false) }
        val filter = remember(filtering) {
            if (!filtering) {
                null
            } else {
                TestConfig.children.filter { it.name.contains("int", ignoreCase = true) }.toSet()
            }
        }

        Column(
            Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button({
                    filtering = !filtering
                }) {
                    Text(if (filtering) "搜索过滤：开" else "搜索过滤：关")
                }
                Text("除 test_char（Char，无 wrapper）外都有真控件；列表 / 映射可增删与拖拽排序")
                DispatchSelfCheck(TestConfig)
                DispatchSelfCheck(IGConfig)
            }

            CompositionLocalProvider(LocalSearchFilter provides filter) {
                ConfigsWrapper(
                    configs = TestConfig.children,
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                )
            }
        }
    }
}

/**
 * 配置页面骨架测试屏：把 devOnly 的 [TestConfig] 整页交给 [ConfigManagerWrapper]。
 *
 * 验证点：左侧分组列表（含 3 层嵌套的 nested / nested2 / vector）、文字左对齐与选中项的
 * `focused` 常驻底色、右下搜索入口（点开后输入 `int` 应平铺出各分组的 int 项，返回可回分组）、
 * 内容区随滚动显隐的叠加滚动条。
 */
fun ConfigManagerTestScreen() = TestScreen {
    Surface(Modifier.fillMaxSize()) {
        ConfigManagerWrapper(manager = TestConfig)
    }
}

/**
 * IGConfig 真实配置页测试屏：`ConfigManagerWrapper(IGConfig)`。
 *
 * 与 [ConfigManagerTestScreen] 的区别是数据来自**真实配置**（快捷键 / 滚动倍率 / 提示 /
 * 屏幕与对话框动画 / 缓动曲线），因此这里是"按类型分发在真实数据上都成立"的验收面：
 * 尤其 `easing_custom`（`CubicBezier`）应显示行内曲线速览并可打开编辑器，而不是落在兜底行。
 */
fun IgConfigManagerTestScreen() = TestScreen {
    Surface(Modifier.fillMaxSize()) {
        ConfigManagerWrapper(manager = IGConfig)
    }
}

/**
 * 分发自检：列出**落到兜底行**（没有命中任何 wrapper）的节点。
 *
 * 不看不知道有没有漏注册 —— `UIWrappers.find` 是纯谓词匹配，这里在组合期算一次即可，
 * 结果直接印在测试屏上；新增配置类型后忘了注册 wrapper 会立刻在这里现形。
 */
@Composable
private fun DispatchSelfCheck(manager: ConfigManager) {
    val fallback = remember(manager) {
        allNodes(manager).filter { UIWrappers.find(it) == null }.map { it.name }
    }
    Text(
        if (fallback.isEmpty()) {
            "${manager.name} 分发自检：全部节点均有 wrapper"
        } else {
            "${manager.name} 分发自检：${fallback.size} 项落到兜底行 → ${fallback.joinToString()}"
        }
    )
}

/** 管理器下的全部节点（含分组行本身）。 */
private fun allNodes(manager: ConfigManager): List<ConfigNode> {
    val nodes = mutableListOf<ConfigNode>()
    fun collect(group: ConfigGroup) {
        group.children.forEach { child ->
            nodes += child
            if (child is ConfigGroup) collect(child)
        }
    }
    collect(manager)
    return nodes
}

/**
 * 列表 / 映射**元素控件**分发的验证点（`TestConfig` 里的对应项）：
 * `test_enum_list`（枚举 → 下拉）、`test_duration_list`（时长 → 时长框）、
 * `test_bezier_list`（曲线 → 行内画布）、`test_int_pair_list`（整数对 → 两个数值框，
 * 证明对列表不再假设分量是字符串）、`test_string_pair_list`（字符串对）。
 */
