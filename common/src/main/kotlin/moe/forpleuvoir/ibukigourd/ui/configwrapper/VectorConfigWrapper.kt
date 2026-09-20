package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import moe.forpleuvoir.ibukigourd.config.item.range
import moe.forpleuvoir.ibukigourd.ui.sokitsu.DoubleField
import moe.forpleuvoir.ibukigourd.ui.sokitsu.FloatField
import moe.forpleuvoir.ibukigourd.ui.sokitsu.IntField
import moe.forpleuvoir.ibukigourd.ui.sokitsu.Text
import moe.forpleuvoir.ibukigourd.util.math.Vector2d
import moe.forpleuvoir.ibukigourd.util.math.Vector2f
import moe.forpleuvoir.ibukigourd.util.math.Vector2i
import moe.forpleuvoir.ibukigourd.util.math.Vector3d
import moe.forpleuvoir.ibukigourd.util.math.Vector3f
import moe.forpleuvoir.ibukigourd.util.math.Vector3i
import moe.forpleuvoir.nebula.config.Config
import org.joml.Vector2dc
import org.joml.Vector2fc
import org.joml.Vector2ic
import org.joml.Vector3dc
import org.joml.Vector3fc
import org.joml.Vector3ic

/**
 * 向量：每个分量一个数值框，各行按分量顺序横向排开。
 *
 * 取值区间取自配置项构建时写入的 `range` metadata（见
 * [moe.forpleuvoir.ibukigourd.config.item.configVector2i] 一族），因此分量框自带上下限。
 *
 * 与旧版的差别：旧版为 6 种向量各写了一份几乎相同的 wrapper（共 344 行），本版只保留
 * **按分量展开**这一条最短路径 —— 上一版还有"分量折叠 / 展开"等展示态，收益不抵复杂度。
 */

/** 单个分量框宽度。 */
private val ComponentWidth: Dp = ConfigControlDefaults.FieldWidth

@Composable
fun Vector2iConfigWrapper(config: Config<Vector2ic>, modifier: Modifier = Modifier) {
    val value by config.asState()
    val range = config.range
    ConfigRowWrapper(config, modifier) {
        IntField(
            value = value.x(),
            leadingIcon = { Text("x") },
            onValueChange = { config.setValue(Vector2i(it, value.y())) },
            valueRange = range?.let { it.first.x()..it.second.x() },
            modifier = Modifier.width(ComponentWidth),
        )
        IntField(
            value = value.y(),
            leadingIcon = { Text("y") },
            onValueChange = { config.setValue(Vector2i(value.x(), it)) },
            valueRange = range?.let { it.first.y()..it.second.y() },
            modifier = Modifier.width(ComponentWidth),
        )
    }
}

@Composable
fun Vector3iConfigWrapper(config: Config<Vector3ic>, modifier: Modifier = Modifier) {
    val value by config.asState()
    val range = config.range
    ConfigRowWrapper(config, modifier) {
        IntField(
            value = value.x(),
            leadingIcon = { Text("x") },
            onValueChange = { config.setValue(Vector3i(it, value.y(), value.z())) },
            valueRange = range?.let { it.first.x()..it.second.x() },
            modifier = Modifier.width(ComponentWidth),
        )
        IntField(
            value = value.y(),
            leadingIcon = { Text("y") },
            onValueChange = { config.setValue(Vector3i(value.x(), it, value.z())) },
            valueRange = range?.let { it.first.y()..it.second.y() },
            modifier = Modifier.width(ComponentWidth),
        )
        IntField(
            value = value.z(),
            leadingIcon = { Text("z") },
            onValueChange = { config.setValue(Vector3i(value.x(), value.y(), it)) },
            valueRange = range?.let { it.first.z()..it.second.z() },
            modifier = Modifier.width(ComponentWidth),
        )
    }
}

@Composable
fun Vector2fConfigWrapper(config: Config<Vector2fc>, modifier: Modifier = Modifier) {
    val value by config.asState()
    val range = config.range
    ConfigRowWrapper(config, modifier) {
        FloatField(
            value = value.x(),
            leadingIcon = { Text("x") },
            onValueChange = { config.setValue(Vector2f(it, value.y())) },
            valueRange = range?.let { it.first.x()..it.second.x() },
            modifier = Modifier.width(ComponentWidth),
        )
        FloatField(
            value = value.y(),
            leadingIcon = { Text("y") },
            onValueChange = { config.setValue(Vector2f(value.x(), it)) },
            valueRange = range?.let { it.first.y()..it.second.y() },
            modifier = Modifier.width(ComponentWidth),
        )
    }
}

@Composable
fun Vector3fConfigWrapper(config: Config<Vector3fc>, modifier: Modifier = Modifier) {
    val value by config.asState()
    val range = config.range
    ConfigRowWrapper(config, modifier) {
        FloatField(
            value = value.x(),
            leadingIcon = { Text("x") },
            onValueChange = { config.setValue(Vector3f(it, value.y(), value.z())) },
            valueRange = range?.let { it.first.x()..it.second.x() },
            modifier = Modifier.width(ComponentWidth),
        )
        FloatField(
            value = value.y(),
            leadingIcon = { Text("y") },
            onValueChange = { config.setValue(Vector3f(value.x(), it, value.z())) },
            valueRange = range?.let { it.first.y()..it.second.y() },
            modifier = Modifier.width(ComponentWidth),
        )
        FloatField(
            value = value.z(),
            leadingIcon = { Text("z") },
            onValueChange = { config.setValue(Vector3f(value.x(), value.y(), it)) },
            valueRange = range?.let { it.first.z()..it.second.z() },
            modifier = Modifier.width(ComponentWidth),
        )
    }
}

@Composable
fun Vector2dConfigWrapper(config: Config<Vector2dc>, modifier: Modifier = Modifier) {
    val value by config.asState()
    val range = config.range
    ConfigRowWrapper(config, modifier) {
        DoubleField(
            value = value.x(),
            leadingIcon = { Text("x") },
            onValueChange = { config.setValue(Vector2d(it, value.y())) },
            valueRange = range?.let { it.first.x()..it.second.x() },
            modifier = Modifier.width(ComponentWidth),
        )
        DoubleField(
            value = value.y(),
            leadingIcon = { Text("y") },
            onValueChange = { config.setValue(Vector2d(value.x(), it)) },
            valueRange = range?.let { it.first.y()..it.second.y() },
            modifier = Modifier.width(ComponentWidth),
        )
    }
}

@Composable
fun Vector3dConfigWrapper(config: Config<Vector3dc>, modifier: Modifier = Modifier) {
    val value by config.asState()
    val range = config.range
    ConfigRowWrapper(config, modifier) {
        DoubleField(
            value = value.x(),
            leadingIcon = { Text("x") },
            onValueChange = { config.setValue(Vector3d(it, value.y(), value.z())) },
            valueRange = range?.let { it.first.x()..it.second.x() },
            modifier = Modifier.width(ComponentWidth),
        )
        DoubleField(
            value = value.y(),
            leadingIcon = { Text("y") },
            onValueChange = { config.setValue(Vector3d(value.x(), it, value.z())) },
            valueRange = range?.let { it.first.y()..it.second.y() },
            modifier = Modifier.width(ComponentWidth),
        )
        DoubleField(
            value = value.z(),
            leadingIcon = { Text("z") },
            onValueChange = { config.setValue(Vector3d(value.x(), value.y(), it)) },
            valueRange = range?.let { it.first.z()..it.second.z() },
            modifier = Modifier.width(ComponentWidth),
        )
    }
}
