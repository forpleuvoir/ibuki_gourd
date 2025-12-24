@file:Suppress("DuplicatedCode", "unused", "FunctionName")

package moe.forpleuvoir.ibukigourd.gui.widget

import moe.forpleuvoir.ibukigourd.gui.base.layout.arrange.Arrangement
import moe.forpleuvoir.ibukigourd.gui.base.modifier.Modifier
import moe.forpleuvoir.ibukigourd.gui.base.modifier.impl.width
import moe.forpleuvoir.ibukigourd.gui.base.scope.ContainerScope
import moe.forpleuvoir.ibukigourd.gui.widget.layout.Row
import moe.forpleuvoir.ibukigourd.gui.widget.layout.RowScope
import moe.forpleuvoir.ibukigourd.gui.widget.text.DoubleEditor
import moe.forpleuvoir.ibukigourd.gui.widget.text.FloatEditor
import moe.forpleuvoir.ibukigourd.gui.widget.text.IntEditor
import moe.forpleuvoir.ibukigourd.gui.widget.text.Text
import moe.forpleuvoir.ibukigourd.text.Literal
import moe.forpleuvoir.ibukigourd.text.withColor
import moe.forpleuvoir.ibukigourd.util.math.copy
import moe.forpleuvoir.ibukigourd.util.state.MutableState
import moe.forpleuvoir.ibukigourd.util.state.mutableStateOf
import moe.forpleuvoir.nebula.common.color.Colors
import org.joml.*

fun ContainerScope.Vector3iEditor(
    vector3i: MutableState<Vector3ic>,
    minValue: Vector3ic = Vector3i(Int.MIN_VALUE),
    maxValue: Vector3ic = Vector3i(Int.MAX_VALUE),
    modifier: Modifier = Modifier,
    editorModifier: Modifier = Modifier.width(60f),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(5f),
    scope: RowScope.(xValue: MutableState<Int>, yValue: MutableState<Int>, zValue: MutableState<Int>) -> Unit = { _, _, _ -> }
) = Row(
    modifier,
    horizontalArrangement
) {

    val xValue = mutableStateOf(vector3i.getValue().x()).apply {
        subscribe {
            vector3i.setValue(vector3i.getValue().copy(x = it))
        }
    }
    val yValue = mutableStateOf(vector3i.getValue().y()).apply {
        subscribe {
            vector3i.setValue(vector3i.getValue().copy(y = it))
        }
    }
    val zValue = mutableStateOf(vector3i.getValue().z()).apply {
        subscribe {
            vector3i.setValue(vector3i.getValue().copy(z = it))
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(2f)) {
        Text(Literal("X").withColor(Colors.RED))
        IntEditor(
            xValue,
            minValue.x()..maxValue.x(),
            modifier = editorModifier,
            editorModifier = { Modifier.weight(1) })
    }

    Row(horizontalArrangement = Arrangement.spacedBy(2f)) {
        Text(Literal("Y").withColor(Colors.LIME))
        IntEditor(
            yValue,
            minValue.y()..maxValue.y(),
            modifier = editorModifier,
            editorModifier = { Modifier.weight(1) })
    }

    Row(horizontalArrangement = Arrangement.spacedBy(2f)) {
        Text(Literal("Z").withColor(Colors.BLUE))
        IntEditor(
            zValue,
            minValue.z()..maxValue.z(),
            modifier = editorModifier,
            editorModifier = { Modifier.weight(1) })
    }
    scope(xValue, yValue, zValue)
}

fun ContainerScope.Vector3fEditor(
    vector3f: MutableState<Vector3fc>,
    minValue: Vector3fc = Vector3f(Float.NEGATIVE_INFINITY),
    maxValue: Vector3fc = Vector3f(Float.POSITIVE_INFINITY),
    modifier: Modifier = Modifier,
    editorModifier: Modifier = Modifier.width(60f),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(5f),
    scope: RowScope.(xValue: MutableState<Float>, yValue: MutableState<Float>, zValue: MutableState<Float>) -> Unit = { _, _, _ -> }
) = Row(
    modifier,
    horizontalArrangement
) {

    val xValue = mutableStateOf(vector3f.getValue().x()).apply {
        subscribe {
            vector3f.setValue(vector3f.getValue().copy(x = it))
        }
    }
    val yValue = mutableStateOf(vector3f.getValue().y()).apply {
        subscribe {
            vector3f.setValue(vector3f.getValue().copy(y = it))
        }
    }
    val zValue = mutableStateOf(vector3f.getValue().z()).apply {
        subscribe {
            vector3f.setValue(vector3f.getValue().copy(z = it))
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(2f)) {
        Text(Literal("X").withColor(Colors.RED))
        FloatEditor(
            xValue,
            minValue.x()..maxValue.x(),
            modifier = editorModifier,
            editorModifier = { Modifier.weight(1) })
    }

    Row(horizontalArrangement = Arrangement.spacedBy(2f)) {
        Text(Literal("Y").withColor(Colors.LIME))
        FloatEditor(
            yValue,
            minValue.y()..maxValue.y(),
            modifier = editorModifier,
            editorModifier = { Modifier.weight(1) })
    }

    Row(horizontalArrangement = Arrangement.spacedBy(2f)) {
        Text(Literal("Z").withColor(Colors.BLUE))
        FloatEditor(
            zValue,
            minValue.z()..maxValue.z(),
            modifier = editorModifier,
            editorModifier = { Modifier.weight(1) })
    }
    scope(xValue, yValue, zValue)
}

fun ContainerScope.Vector3dEditor(
    vector3d: MutableState<Vector3dc>,
    minValue: Vector3dc = Vector3d(Double.NEGATIVE_INFINITY),
    maxValue: Vector3dc = Vector3d(Double.POSITIVE_INFINITY),
    modifier: Modifier = Modifier,
    editorModifier: Modifier = Modifier.width(60f),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(5f),
    scope: RowScope.(xValue: MutableState<Double>, yValue: MutableState<Double>, zValue: MutableState<Double>) -> Unit = { _, _, _ -> }
) = Row(
    modifier,
    horizontalArrangement
) {

    val xValue = mutableStateOf(vector3d.getValue().x()).apply {
        subscribe {
            vector3d.setValue(vector3d.getValue().copy(x = it))
        }
    }
    val yValue = mutableStateOf(vector3d.getValue().y()).apply {
        subscribe {
            vector3d.setValue(vector3d.getValue().copy(y = it))
        }
    }
    val zValue = mutableStateOf(vector3d.getValue().z()).apply {
        subscribe {
            vector3d.setValue(vector3d.getValue().copy(z = it))
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(2f)) {
        Text(Literal("X").withColor(Colors.RED))
        DoubleEditor(
            xValue,
            minValue.x()..maxValue.x(),
            modifier = editorModifier,
            editorModifier = { Modifier.weight(1) })
    }

    Row(horizontalArrangement = Arrangement.spacedBy(2f)) {
        Text(Literal("Y").withColor(Colors.LIME))
        DoubleEditor(
            yValue,
            minValue.y()..maxValue.y(),
            modifier = editorModifier,
            editorModifier = { Modifier.weight(1) })
    }

    Row(horizontalArrangement = Arrangement.spacedBy(2f)) {
        Text(Literal("Z").withColor(Colors.BLUE))
        DoubleEditor(
            zValue,
            minValue.z()..maxValue.z(),
            modifier = editorModifier,
            editorModifier = { Modifier.weight(1) })
    }
    scope(xValue, yValue, zValue)
}

fun ContainerScope.Vector2iEditor(
    vector2i: MutableState<Vector2ic>,
    minValue: Vector2ic = Vector2i(Int.MIN_VALUE),
    maxValue: Vector2ic = Vector2i(Int.MAX_VALUE),
    modifier: Modifier = Modifier,
    editorModifier: Modifier = Modifier.width(60f),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(5f),
    scope: RowScope.(xValue: MutableState<Int>, yValue: MutableState<Int>) -> Unit = { _, _ -> }
) = Row(
    modifier,
    horizontalArrangement
) {

    val xValue = mutableStateOf(vector2i.getValue().x()).apply {
        subscribe {
            vector2i.setValue(vector2i.getValue().copy(x = it))
        }
    }
    val yValue = mutableStateOf(vector2i.getValue().y()).apply {
        subscribe {
            vector2i.setValue(vector2i.getValue().copy(y = it))
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(2f)) {
        Text(Literal("X").withColor(Colors.RED))
        IntEditor(
            xValue,
            minValue.x()..maxValue.x(),
            modifier = editorModifier,
            editorModifier = { Modifier.weight(1) })
    }

    Row(horizontalArrangement = Arrangement.spacedBy(2f)) {
        Text(Literal("Y").withColor(Colors.LIME))
        IntEditor(
            yValue,
            minValue.y()..maxValue.y(),
            modifier = editorModifier,
            editorModifier = { Modifier.weight(1) })
    }
    scope(xValue, yValue)
}

fun ContainerScope.Vector2fEditor(
    vector2f: MutableState<Vector2fc>,
    minValue: Vector2fc = Vector2f(Float.NEGATIVE_INFINITY),
    maxValue: Vector2fc = Vector2f(Float.POSITIVE_INFINITY),
    modifier: Modifier = Modifier,
    editorModifier: Modifier = Modifier.width(60f),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(5f),
    scope: RowScope.(xValue: MutableState<Float>, yValue: MutableState<Float>) -> Unit = { _, _ -> }
) = Row(
    modifier,
    horizontalArrangement
) {

    val xValue = mutableStateOf(vector2f.getValue().x()).apply {
        subscribe {
            vector2f.setValue(vector2f.getValue().copy(x = it))
        }
    }
    val yValue = mutableStateOf(vector2f.getValue().y()).apply {
        subscribe {
            vector2f.setValue(vector2f.getValue().copy(y = it))
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(2f)) {
        Text(Literal("X").withColor(Colors.RED))
        FloatEditor(
            xValue,
            minValue.x()..maxValue.x(),
            modifier = editorModifier,
            editorModifier = { Modifier.weight(1) })
    }

    Row(horizontalArrangement = Arrangement.spacedBy(2f)) {
        Text(Literal("Y").withColor(Colors.LIME))
        FloatEditor(
            yValue,
            minValue.y()..maxValue.y(),
            modifier = editorModifier,
            editorModifier = { Modifier.weight(1) })
    }
    scope(xValue, yValue)
}

fun ContainerScope.Vector2dEditor(
    vector2d: MutableState<Vector2dc>,
    minValue: Vector2dc = Vector2d(Double.NEGATIVE_INFINITY),
    maxValue: Vector2dc = Vector2d(Double.POSITIVE_INFINITY),
    modifier: Modifier = Modifier,
    editorModifier: Modifier = Modifier.width(60f),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(5f),
    scope: RowScope.(xValue: MutableState<Double>, yValue: MutableState<Double>) -> Unit = { _, _ -> }
) = Row(
    modifier,
    horizontalArrangement
) {

    val xValue = mutableStateOf(vector2d.getValue().x()).apply {
        subscribe {
            vector2d.setValue(vector2d.getValue().copy(x = it))
        }
    }
    val yValue = mutableStateOf(vector2d.getValue().y()).apply {
        subscribe {
            vector2d.setValue(vector2d.getValue().copy(y = it))
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(2f)) {
        Text(Literal("X").withColor(Colors.RED))
        DoubleEditor(
            xValue,
            minValue.x()..maxValue.x(),
            modifier = editorModifier,
            editorModifier = { Modifier.weight(1) })
    }

    Row(horizontalArrangement = Arrangement.spacedBy(2f)) {
        Text(Literal("Y").withColor(Colors.LIME))
        DoubleEditor(
            yValue,
            minValue.y()..maxValue.y(),
            modifier = editorModifier,
            editorModifier = { Modifier.weight(1) })
    }
    scope(xValue, yValue)
}