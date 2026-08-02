package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import moe.forpleuvoir.ibukigourd.config.translateText
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.text.InlineStyleText
import moe.forpleuvoir.ibukigourd.text.plainText
import moe.forpleuvoir.ibukigourd.ui.icon.Icons
import moe.forpleuvoir.ibukigourd.ui.icon.defaults.Add
import moe.forpleuvoir.ibukigourd.ui.icon.defaults.ArrowRightAlt
import moe.forpleuvoir.ibukigourd.ui.icon.defaults.EditNote
import moe.forpleuvoir.ibukigourd.ui.platformcontext.IGCompositionLocalProvider
import moe.forpleuvoir.ibukigourd.ui.preset.DragHandle
import moe.forpleuvoir.ibukigourd.ui.preset.FlexibleDialog
import moe.forpleuvoir.ibukigourd.ui.preset.RemoveConfirmButton
import moe.forpleuvoir.ibukigourd.ui.preset.Text
import moe.forpleuvoir.ibukigourd.ui.preset.modifier.fabVisibilityAnimation
import moe.forpleuvoir.ibukigourd.ui.preset.state.rememberFabVisibilityByScroll
import moe.forpleuvoir.ibukigourd.ui.util.Keyed
import moe.forpleuvoir.ibukigourd.ui.util.rememberKeyedList
import moe.forpleuvoir.ibukigourd.util.moveElement
import moe.forpleuvoir.nebula.config.item.ConfigMap
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

data class MapEntry<K, V>(
    val key: K,
    val value: V,
)

private typealias KeyedMapEntry<V> = Keyed<MapEntry<String, V>>

object MapConfigWrapperDefaults {

    @Composable
    fun <V : Any> RowWrapper(
        config: ConfigMap<V>,
        modifier: Modifier = Modifier,
        editAction: () -> Unit,
    ) = ConfigRowWrapper(config, modifier = modifier) {
        Row(
            modifier = Modifier.size(ConfigRowWrapper.entrySize),
            horizontalArrangement = Arrangement.spacedBy(ConfigRowWrapper.spacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val displaySize by config.asDerivedState { it.size }
            AssistChip(
                {},
                {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(IGLang.ConfigWrapper.mapConfigWrapperText(displaySize), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    }
                },
                modifier = Modifier.weight(1f).height(40.dp)
            )
            IconButton(onClick = editAction) {
                Icon(Icons.EditNote, IGLang.Misc.edit.plainText)
            }
        }
    }

    @Composable
    fun <V : Any> EditDialog(
        config: ConfigMap<V>,
        modifier: Modifier = Modifier,
        onDismissRequest: () -> Unit,
        title: @Composable (() -> Unit)? = { Text(InlineStyleText(config.translateText.plainText)) },
        content: @Composable (data: SnapshotStateList<KeyedMapEntry<V>>) -> Unit
    ) {
        //编辑中的映射 确认之后写入config
        val editingValue = rememberKeyedList(config.entries.map { MapEntry(it.key, it.value) })
        FlexibleDialog(
            onDismissRequest = onDismissRequest,
            title = title,
            modifier = modifier,
            onConfirmRequest = {
                config.clear()
                editingValue.forEach { (_, entry) ->
                    val (key, value) = entry
                    config[key] = value
                }
                true
            },
            content = { content(editingValue) }
        )
    }

    @Composable
    fun EditDialogContent(
        modifier: Modifier = Modifier,
        addDialog: @Composable (onDismissRequest: () -> Unit) -> Unit,
        header: @Composable (ColumnScope.() -> Unit) = { EditDialogContentHeader() },
        content: @Composable (ColumnScope.(LazyListState) -> Unit)
    ) = Box(modifier) {
        val lazyListState = rememberLazyListState()
        Column(modifier = Modifier.fillMaxSize()) {
            header()
            content(lazyListState)
        }
        var showDialog by remember { mutableStateOf(false) }
        if (showDialog) addDialog { showDialog = false }
        FloatingActionButton(
            onClick = { showDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
                .size(40.dp)
                .fabVisibilityAnimation(rememberFabVisibilityByScroll(lazyListState))
        ) {
            Icon(Icons.Add, IGLang.Misc.add.plainText)
        }
    }

    val LocalHeaderHeight = staticCompositionLocalOf { 40.dp }

    val LocalKeyColumnWeight = staticCompositionLocalOf { 3f }

    val LocalValueColumnWeight = staticCompositionLocalOf { 5f }

    val LocalColumnSpacing = staticCompositionLocalOf { 8.dp }

    val LocalMoveColumnWidth = staticCompositionLocalOf { 60.dp }

    val LocalRemoveColumnWidth = staticCompositionLocalOf { 60.dp }

    @Composable
    fun RowScope.MoveColumn(
        content: @Composable BoxScope.() -> Unit
    ) = Box(
        Modifier.width(LocalMoveColumnWidth.current),
        contentAlignment = Alignment.Center,
        content = content
    )

    @Composable
    fun RowScope.RemoveColumn(
        content: @Composable BoxScope.() -> Unit
    ) = Box(
        Modifier.width(LocalRemoveColumnWidth.current),
        contentAlignment = Alignment.Center,
        content = content
    )

    @Composable
    fun EditDialogContentHeader(
        modifier: Modifier = Modifier
            .fillMaxWidth()
            .height(LocalHeaderHeight.current)
            .padding(top = 8.dp, bottom = 4.dp),
        horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
        verticalAlignment: Alignment.Vertical = Alignment.Bottom,
        moveHeader: @Composable (RowScope.() -> Unit)? = {
            MoveColumn {
                Text(IGLang.ConfigWrapper.move)
            }
        },
        keyHeader: @Composable BoxScope.() -> Unit = {
            Text(IGLang.ConfigWrapper.mapKey)
        },
        valueHeader: @Composable BoxScope.() -> Unit = {
            Text(IGLang.ConfigWrapper.mapValue)
        },
        removeHeader: @Composable RowScope.() -> Unit = {
            RemoveColumn {
                Text(IGLang.Misc.remove)
            }
        },
        divider: @Composable (() -> Unit)? = { HorizontalDivider() }
    ) {
        Row(
            modifier,
            horizontalArrangement,
            verticalAlignment,
        ) {
            ProvideTextStyle(MaterialTheme.typography.labelSmall) {
                //move
                moveHeader?.let {
                    it()
                    Spacer(Modifier.width(LocalColumnSpacing.current))
                }
                //key
                Box(
                    Modifier.weight(LocalKeyColumnWeight.current),
                    contentAlignment = Alignment.Center
                ) {
                    keyHeader()
                }
                Spacer(Modifier.width(LocalColumnSpacing.current))
                //value
                Box(
                    Modifier.weight(LocalValueColumnWeight.current),
                    contentAlignment = Alignment.Center
                ) {
                    valueHeader()
                }
                Spacer(Modifier.width(LocalColumnSpacing.current))
                //remove
                removeHeader()
            }
        }
        divider?.invoke()
    }

    @Composable
    fun <V : Any> EditDialogContentList(
        data: SnapshotStateList<KeyedMapEntry<V>>,
        key: (KeyedMapEntry<V>) -> Any = { it.key },
        modifier: Modifier = Modifier,
        lazyListState: LazyListState = rememberLazyListState(),
        enableElementMove: Boolean = true,
        verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp),
        horizontalAlignment: Alignment.Horizontal = Alignment.Start,
        entryRowModifier: Modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        keyWrapper: @Composable RowScope.(key: String, (newKey: String) -> Unit) -> Unit = { key, onKeyChange ->
            KeyWrapper(
                key,
                onKeyChange,
                { newKey -> key == newKey || data.any { it.value.key == newKey } },
                modifier = Modifier.weight(LocalKeyColumnWeight.current)
            )
        },
        valueWrapper: @Composable RowScope.(value: V, (newValue: V) -> Unit) -> Unit
    ) {

        Box(
            modifier = modifier,
        ) {
            val hapticFeedback = LocalHapticFeedback.current
            val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
                data.moveElement(from.index, to.index)
                hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
            }

            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(top = 8.dp),
                verticalArrangement = verticalArrangement,
                horizontalAlignment = horizontalAlignment,
            ) {
                itemsIndexed(data, key = { _, entry -> key(entry) }) { index, entry ->
                    ReorderableItem(reorderableLazyListState, key = key(entry)) { isDragging ->
                        val scale by animateFloatAsState(if (isDragging) 1.015f else 1.0f)
                        val (mapKey, value) = data[index].value
                        val handleInteraction = remember { MutableInteractionSource() }
                        val handleHovered by handleInteraction.collectIsHoveredAsState()

                        Row(
                            modifier = entryRowModifier.scale(scale),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            //移动手柄
                            if (enableElementMove) {
                                MoveColumn {
                                    DragHandle(hapticFeedback, handleInteraction, handleHovered, isDragging)
                                }
                                Spacer(Modifier.width(LocalColumnSpacing.current))
                            }

                            //Key包装
                            keyWrapper(mapKey) { newKey ->
                                data[index] = Keyed(data[index].key, data[index].value.copy(key = newKey))
                            }
                            Spacer(Modifier.width(LocalColumnSpacing.current))

                            //Value包装
                            valueWrapper(value) { newValue ->
                                data[index] = Keyed(data[index].key, data[index].value.copy(value = newValue))
                            }
                            Spacer(Modifier.width(LocalColumnSpacing.current))
                            //移除按钮
                            RemoveColumn {
                                RemoveConfirmButton(mapKey, { data.removeAt(index) })
                            }
                        }
                    }
                }
            }

            VerticalScrollbar(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight(),
                adapter = rememberScrollbarAdapter(lazyListState),
            )
        }
    }

    @Composable
    fun KeyWrapper(
        key: String,
        onKeyChange: (newKey: String) -> Unit,
        checkKeyDuplicate: (key: String) -> Boolean,
        keyDisplayer: @Composable (key: String) -> Unit = { Text(key, maxLines = 1) },
        modifier: Modifier = Modifier,
    ) {
        var showKeyEditDialog by remember { mutableStateOf(false) }

        AssistChip(
            {},
            label = { keyDisplayer(key) },
            trailingIcon = {
                IconButton(onClick = { showKeyEditDialog = true }) {
                    Icon(Icons.EditNote, contentDescription = null)
                }
            },
            modifier = modifier
        )

        if (showKeyEditDialog) {
            val newKey = rememberTextFieldState(key)
            val isDuplicate = remember(newKey.text.toString()) {
                checkKeyDuplicate(newKey.text.toString())
            }
            AlertDialog(
                onDismissRequest = { showKeyEditDialog = false },
                properties = DialogProperties(usePlatformDefaultWidth = false),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(IGLang.Misc.edit)
                        Icon(Icons.ArrowRightAlt, null)
                        Text(key)
                    }
                },
                text = {
                    IGCompositionLocalProvider {
                        OutlinedTextField(
                            newKey,
                            modifier = Modifier.fillMaxWidth().height(60.dp),
                            lineLimits = TextFieldLineLimits.SingleLine,
                            isError = isDuplicate,
                            label = {
                                if (isDuplicate) Text(IGLang.ConfigWrapper.keyExists(newKey.text.toString()))
                                else Text(IGLang.ConfigWrapper.mapKey)
                            },
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (!isDuplicate) {
                                onKeyChange(newKey.text.toString())
                                showKeyEditDialog = false
                            }
                        },
                        enabled = !isDuplicate,
                    ) {
                        Text(IGLang.Misc.confirm)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showKeyEditDialog = false }) {
                        Text(IGLang.Misc.cancel)
                    }
                },
            )
        }
    }
}
