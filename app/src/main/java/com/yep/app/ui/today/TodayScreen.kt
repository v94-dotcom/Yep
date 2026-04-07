package com.yep.app.ui.today

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yep.app.data.entities.Item
import com.yep.app.ui.settings.SettingsScreen
import com.yep.app.ui.theme.GreenPrimary
import com.yep.app.ui.theme.NeutralGray
import com.yep.app.util.DateUtils
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    onNavigateToCamera: (itemId: String, itemLabel: String) -> Unit = { _, _ -> },
    onNavigateToPhoto: (photoPaths: List<String>, itemLabel: String, confirmedAt: Long) -> Unit = { _, _, _ -> },
    viewModel: TodayViewModel = hiltViewModel()
) {
    val items by viewModel.items.collectAsState()
    val confirmations by viewModel.todayConfirmations.collectAsState()
    val expandedItemId by viewModel.expandedItemId.collectAsState()
    val isEditMode by viewModel.isEditMode.collectAsState()

    val confirmedCount = confirmations.size
    val totalCount = items.size

    var newItemText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val addFieldFocus = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()
    var showSettings by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val shakeOffset = remember { Animatable(0f) }
    var editButtonFlash by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.shakeEditHeader.collect {
            editButtonFlash = true
            repeat(4) { i ->
                shakeOffset.animateTo(
                    if (i % 2 == 0) 10f else -10f,
                    spring(stiffness = Spring.StiffnessHigh)
                )
            }
            shakeOffset.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
            editButtonFlash = false
        }
    }

    BackHandler(enabled = isEditMode) {
        viewModel.toggleEditMode()
    }

    // Local mutable list drives the LazyColumn so drags are instant.
    // Synced from DB whenever we're not mid-drag.
    val localItems = remember { mutableStateListOf<Item>() }
    var isDraggingAny by remember { mutableStateOf(false) }

    LaunchedEffect(items) {
        if (!isDraggingAny) {
            localItems.clear()
            localItems.addAll(items)
        }
    }

    val density = LocalDensity.current
    val imeBottom = WindowInsets.ime.getBottom(density)
    LaunchedEffect(imeBottom) {
        if (imeBottom == 0) focusManager.clearFocus()
    }

    val lazyListState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(lazyListState) { from, to ->
        localItems.apply { add(to.index, removeAt(from.index)) }
    }

    fun submitNewItem() {
        if (newItemText.isNotBlank()) {
            viewModel.addItem(newItemText)
            newItemText = ""
        }
        focusManager.clearFocus()
    }

    if (showSettings) {
        ModalBottomSheet(
            onDismissRequest = { showSettings = false },
            sheetState = sheetState
        ) {
            SettingsScreen()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .imePadding()
            .padding(horizontal = 16.dp)
    ) {
        // Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
                .offset(x = shakeOffset.value.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Yep.",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.weight(1f))
            if (totalCount > 0) {
                Surface(
                    shape = RoundedCornerShape(99.dp),
                    color = if (confirmedCount == totalCount) GreenPrimary
                            else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "$confirmedCount/$totalCount",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (confirmedCount == totalCount) Color.White else NeutralGray,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }
            IconButton(
                onClick = { showSettings = true },
                enabled = !isEditMode
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = if (isEditMode) NeutralGray.copy(alpha = 0.35f) else NeutralGray,
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(onClick = { viewModel.toggleEditMode() }) {
                Icon(
                    imageVector = if (isEditMode) Icons.Default.Done else Icons.Default.Edit,
                    contentDescription = if (isEditMode) "Done editing" else "Edit items",
                    tint = when {
                        isEditMode && editButtonFlash -> Color(0xFFE57310)
                        isEditMode -> GreenPrimary
                        else -> NeutralGray
                    },
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Date subtitle
        Text(
            text = DateUtils.todayDisplay(),
            style = MaterialTheme.typography.bodyMedium,
            color = NeutralGray,
            modifier = Modifier.padding(bottom = if (isEditMode) 4.dp else 16.dp)
        )
        AnimatedVisibility(visible = isEditMode) {
            Text(
                text = "Drag to reorder, uncheck, or delete items",
                style = MaterialTheme.typography.bodySmall,
                color = NeutralGray.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // Item list
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(localItems, key = { it.id }) { item ->
                ReorderableItem(
                    reorderState,
                    key = item.id,
                    modifier = Modifier.animateItem()
                ) { _ ->
                    val confirmation = confirmations.find { it.itemId == item.id }
                    ItemCard(
                        item = item,
                        confirmation = confirmation,
                        isExpanded = expandedItemId == item.id,
                        isEditMode = isEditMode,
                        dragHandleModifier = if (isEditMode) Modifier.draggableHandle(
                            onDragStarted = { isDraggingAny = true },
                            onDragStopped = {
                                coroutineScope.launch {
                                    viewModel.reorderItems(localItems.toList())
                                    isDraggingAny = false
                                }
                            }
                        ) else Modifier,
                        onClick = { viewModel.toggleExpand(item.id) },
                        onConfirm = { viewModel.confirmItem(item.id) },
                        onConfirmWithPhoto = {
                            viewModel.collapseAll()
                            onNavigateToCamera(item.id, item.label)
                        },
                        onDelete = { viewModel.deleteItem(item) },
                        onUncheck = { viewModel.uncheckItem(item.id) },
                        onViewPhoto = {
                            val paths = confirmation?.photoPaths ?: return@ItemCard
                            onNavigateToPhoto(paths, item.label, confirmation.confirmedAt)
                        }
                    )
                }
            }
        }

        // Add item input (hidden during edit mode)
        AnimatedVisibility(visible = !isEditMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newItemText,
                    onValueChange = { newItemText = it },
                    placeholder = { Text("Add item…") },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(addFieldFocus),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { submitNewItem() })
                )
                Button(
                    onClick = { submitNewItem() },
                    enabled = newItemText.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add item",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
