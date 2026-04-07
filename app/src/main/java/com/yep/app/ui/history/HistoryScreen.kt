package com.yep.app.ui.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yep.app.data.entities.Confirmation
import com.yep.app.data.entities.Item
import com.yep.app.ui.theme.GreenPrimary
import com.yep.app.ui.theme.NeutralGray
import com.yep.app.util.DateUtils

@Composable
fun HistoryScreen(
    onNavigateToPhoto: (photoPath: String, itemLabel: String, confirmedAt: Long) -> Unit = { _, _, _ -> },
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val items by viewModel.items.collectAsState()
    val historyDays by viewModel.historyDays.collectAsState()

    val todayDay = historyDays.firstOrNull()
    val pastDays = if (historyDays.size > 1) historyDays.drop(1) else emptyList()

    var expandedDate by rememberSaveable { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Text(
            text = "History",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            if (todayDay != null && items.isNotEmpty()) {
                item {
                    TodaySummaryCard(day = todayDay, items = items)
                }
            }

            items(pastDays.filter { it.confirmedItemIds.isNotEmpty() }) { day ->
                val isExpanded = expandedDate == day.date
                HistoryDayCard(
                    day = day,
                    isExpanded = isExpanded,
                    onToggle = { expandedDate = if (isExpanded) null else day.date },
                    onViewPhoto = onNavigateToPhoto
                )
            }
        }
    }
}

@Composable
private fun TodaySummaryCard(day: HistoryDay, items: List<Item>) {
    val confirmedCount = day.confirmedItemIds.size
    val total = day.totalItems
    val percentage = if (total > 0) (confirmedCount * 100 / total) else 0

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Today — ${DateUtils.formatHistoryDate(day.date)}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "$confirmedCount of $total confirmed",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            if (total > 0) {
                Text(
                    text = "$percentage% complete",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                )
            }
            Spacer(Modifier.height(12.dp))
            DotRow(confirmedItemIds = day.confirmedItemIds, itemIds = items.map { it.id })
        }
    }
}

@Composable
private fun HistoryDayCard(
    day: HistoryDay,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onViewPhoto: (photoPath: String, itemLabel: String, confirmedAt: Long) -> Unit
) {
    val confirmedCount = day.confirmedItemIds.size
    val total = day.totalItems
    val isAllClear = total > 0 && confirmedCount == total
    val canExpand = day.hasSnapshot

    val chevronAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(200),
        label = "chevron"
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (canExpand) Modifier.clickable { onToggle() } else Modifier),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = DateUtils.formatHistoryDate(day.date),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$confirmedCount of $total",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeutralGray
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isAllClear) {
                        Text(
                            text = "All clear",
                            style = MaterialTheme.typography.labelSmall,
                            color = GreenPrimary
                        )
                    }
                    if (canExpand) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = NeutralGray,
                            modifier = Modifier
                                .size(20.dp)
                                .rotate(chevronAngle)
                        )
                    }
                }
            }

            if (!isExpanded) {
                Spacer(Modifier.height(10.dp))
                val dotItemIds = if (canExpand) {
                    day.snapshotItems!!.map { it.itemId }
                } else {
                    day.confirmedItemIds.toList()
                }
                DotRow(confirmedItemIds = day.confirmedItemIds, itemIds = dotItemIds)
            }

            if (canExpand) {
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically(animationSpec = tween(200)),
                    exit = shrinkVertically(animationSpec = tween(200))
                ) {
                    Column {
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        Spacer(Modifier.height(4.dp))
                        day.snapshotItems!!.forEach { snapshotItem ->
                            HistoryItemRow(
                                itemLabel = snapshotItem.itemLabel,
                                confirmation = day.confirmations[snapshotItem.itemId],
                                onViewPhoto = { path, confirmedAt ->
                                    onViewPhoto(path, snapshotItem.itemLabel, confirmedAt)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryItemRow(
    itemLabel: String,
    confirmation: Confirmation?,
    onViewPhoto: (photoPath: String, confirmedAt: Long) -> Unit
) {
    val context = LocalContext.current
    val hasPhoto = confirmation?.photoPath != null

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .then(if (hasPhoto) Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onViewPhoto(confirmation!!.photoPath!!, confirmation.confirmedAt)
            } else Modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = itemLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (confirmation != null)
                    "Yep — ${DateUtils.formatTime(context, confirmation.confirmedAt)}"
                else
                    "Not confirmed",
                style = MaterialTheme.typography.bodySmall,
                color = if (confirmation != null) GreenPrimary else NeutralGray
            )
        }
        if (hasPhoto) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = GreenPrimary.copy(alpha = 0.12f),
                modifier = Modifier.clickable {
                    onViewPhoto(confirmation!!.photoPath!!, confirmation.confirmedAt)
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "View photo",
                        tint = GreenPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "photo",
                        style = MaterialTheme.typography.labelSmall,
                        color = GreenPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun DotRow(confirmedItemIds: Set<String>, itemIds: List<String>) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        itemIds.forEach { id ->
            val confirmed = id in confirmedItemIds
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (confirmed) GreenPrimary else MaterialTheme.colorScheme.outline)
            )
        }
    }
}
