package com.yep.app.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yep.app.data.entities.Item
import com.yep.app.ui.theme.BorderGray
import com.yep.app.ui.theme.Charcoal
import com.yep.app.ui.theme.GreenDark
import com.yep.app.ui.theme.GreenDarkest
import com.yep.app.ui.theme.GreenPrimary
import com.yep.app.ui.theme.Mint
import com.yep.app.ui.theme.NeutralGray
import com.yep.app.ui.theme.Surface as AppSurface
import com.yep.app.util.DateUtils

@Composable
fun HistoryScreen(viewModel: HistoryViewModel = hiltViewModel()) {
    val items by viewModel.items.collectAsState()
    val historyDays by viewModel.historyDays.collectAsState()

    val todayDay = historyDays.firstOrNull()
    val pastDays = if (historyDays.size > 1) historyDays.drop(1) else emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppSurface)
    ) {
        // Header
        Text(
            text = "History",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Charcoal,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp, end = 16.dp, bottom = 16.dp
            )
        ) {
            // Today's summary card
            if (todayDay != null && items.isNotEmpty()) {
                item {
                    TodaySummaryCard(day = todayDay, items = items)
                }
            }

            // Past days
            items(pastDays.filter { it.totalItems > 0 || it.confirmedItemIds.isNotEmpty() }) { day ->
                HistoryDayCard(day = day, items = items)
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
        color = Mint,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Today — ${DateUtils.formatHistoryDate(day.date)}",
                style = MaterialTheme.typography.labelMedium,
                color = GreenDark
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "$confirmedCount of $total confirmed",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = GreenDarkest
            )
            if (total > 0) {
                Text(
                    text = "$percentage% complete",
                    style = MaterialTheme.typography.bodySmall,
                    color = GreenDark
                )
            }
            Spacer(Modifier.height(12.dp))
            DotRow(confirmedItemIds = day.confirmedItemIds, items = items)
        }
    }
}

@Composable
private fun HistoryDayCard(day: HistoryDay, items: List<Item>) {
    val confirmedCount = day.confirmedItemIds.size
    val total = day.totalItems
    val isAllClear = total > 0 && confirmedCount == total

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderGray, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = DateUtils.formatHistoryDate(day.date),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = Charcoal
                )
                if (isAllClear) {
                    Text(
                        text = "All clear",
                        style = MaterialTheme.typography.labelSmall,
                        color = GreenPrimary
                    )
                }
            }
            Text(
                text = "$confirmedCount of $total",
                style = MaterialTheme.typography.bodySmall,
                color = NeutralGray
            )
            if (items.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                DotRow(confirmedItemIds = day.confirmedItemIds, items = items)
            }
        }
    }
}

@Composable
private fun DotRow(confirmedItemIds: Set<String>, items: List<Item>) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        items.forEach { item ->
            val confirmed = item.id in confirmedItemIds
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (confirmed) GreenPrimary else BorderGray)
            )
        }
    }
}
