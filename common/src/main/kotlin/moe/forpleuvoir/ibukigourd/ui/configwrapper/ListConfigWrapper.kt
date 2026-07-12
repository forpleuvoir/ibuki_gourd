package moe.forpleuvoir.ibukigourd.ui.configwrapper

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import moe.forpleuvoir.ibukigourd.config.translateText
import moe.forpleuvoir.ibukigourd.lang.IGLang
import moe.forpleuvoir.ibukigourd.text.InlineStyleText
import moe.forpleuvoir.ibukigourd.text.plainText
import moe.forpleuvoir.ibukigourd.ui.icon.Icons
import moe.forpleuvoir.ibukigourd.ui.icon.default.Add
import moe.forpleuvoir.ibukigourd.ui.icon.default.DragHandle
import moe.forpleuvoir.ibukigourd.ui.icon.default.DragIndicator
import moe.forpleuvoir.ibukigourd.ui.icon.default.EditNote
import moe.forpleuvoir.ibukigourd.ui.preset.DragHandle
import moe.forpleuvoir.ibukigourd.ui.preset.FlexibleDialog
import moe.forpleuvoir.ibukigourd.ui.preset.RemoveConfirmButton
import moe.forpleuvoir.ibukigourd.ui.preset.Text
import moe.forpleuvoir.ibukigourd.ui.preset.modifier.fabVisibilityAnimation
import moe.forpleuvoir.ibukigourd.ui.preset.state.rememberFabVisibilityByScroll
import moe.forpleuvoir.ibukigourd.util.moveElement
import moe.forpleuvoir.nebula.config.item.ConfigList
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

object ListConfigWrapperDefaults {

    @Composable
    fun <E : Any> RowWrapper(
        config: ConfigList<E>,
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
                        Text(IGLang.ConfigWrapper.listConfigWrapperText(displaySize), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
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
    fun <E : Any> EditDialog(
        config: ConfigList<E>,
        modifier: Modifier = Modifier,
        onDismissRequest: () -> Unit,
        title: @Composable (() -> Unit)? = { Text(InlineStyleText(config.translateText.plainText)) },
        content: @Composable (data: SnapshotStateList<E>) -> Unit
    ) {
        val editingValue = remember { config.toList().toMutableStateList() }
        FlexibleDialog(
            onDismissRequest = onDismissRequest,
            title = title,
            modifier = modifier,
            onConfirmRequest = {
                config.clear()
                editingValue.forEach { config.add(it) }
                true
            },
            content = { content(editingValue) }
        )
    }

    @Composable
    fun <C : Any, E : Any> EditDialog(
        config: ConfigList<C>,
        editingValue: SnapshotStateList<E>,
        modifier: Modifier = Modifier,
        onDismissRequest: () -> Unit,
        title: @Composable (() -> Unit)? = { Text(InlineStyleText(config.translateText.plainText)) },
        onConfirmRequest: (data: SnapshotStateList<E>) -> Boolean,
        content: @Composable (data: SnapshotStateList<E>) -> Unit
    ) {
        FlexibleDialog(
            onDismissRequest = onDismissRequest,
            title = title,
            modifier = modifier,
            onConfirmRequest = { onConfirmRequest(editingValue) },
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
        contentHeader: @Composable BoxScope.() -> Unit = {
            Text(IGLang.Misc.content)
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
                moveHeader?.let {
                    it()
                    Spacer(Modifier.width(LocalColumnSpacing.current))
                }
                Box(
                    Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    contentHeader()
                }
                Spacer(Modifier.width(LocalColumnSpacing.current))
                removeHeader()
            }
        }
        divider?.invoke()
    }

    @Composable
    fun <E : Any> EditDialogContentList(
        data: SnapshotStateList<E>,
        key: (E) -> Any,
        modifier: Modifier = Modifier,
        lazyListState: LazyListState = rememberLazyListState(),
        enableElementMove: Boolean = true,
        verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp),
        horizontalAlignment: Alignment.Horizontal = Alignment.Start,
        entryRowModifier: Modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        element: @Composable RowScope.(value: E, (newValue: E) -> Unit) -> Unit,
    ) {
        Box(modifier = modifier) {
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
                itemsIndexed(data, key = { _, e -> key(e) }) { index, entry ->
                    ReorderableItem(reorderableLazyListState, key = key(entry)) { isDragging ->
                        val scale by animateFloatAsState(if (isDragging) 1.015f else 1.0f)
                        val handleInteraction = remember { MutableInteractionSource() }
                        val handleHovered by handleInteraction.collectIsHoveredAsState()

                        Row(
                            modifier = entryRowModifier.scale(scale),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (enableElementMove) {
                                MoveColumn {
                                    DragHandle(hapticFeedback, handleInteraction, handleHovered, isDragging)
                                }
                                Spacer(Modifier.width(LocalColumnSpacing.current))
                            }

                            element(entry) {
                                data[index] = it
                            }

                            Spacer(Modifier.width(LocalColumnSpacing.current))

                            RemoveColumn {
                                RemoveConfirmButton(
                                    "$index",
                                    { data.removeAt(index) }
                                )
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
}

