package com.yep.app.ui.streaks

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yep.app.ui.theme.BorderGray
import com.yep.app.ui.theme.Charcoal
import com.yep.app.ui.theme.GreenDark
import com.yep.app.ui.theme.GreenDarkest
import com.yep.app.ui.theme.GreenPrimary
import com.yep.app.ui.theme.Mint
import com.yep.app.ui.theme.NeutralGray
import com.yep.app.ui.theme.Surface
import com.yep.app.ui.theme.WarmGrayLight

@Composable
fun StreaksScreen(
    viewModel: StreaksViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "Streaks",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = Charcoal
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Large streak circle
        StreakCircle(streak = uiState.currentStreak)

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Current streak",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = Charcoal
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = when {
                uiState.currentStreak == 0 -> "Start today — one tap is all it takes."
                uiState.currentStreak < 3 -> "Nice start. Keep it going."
                uiState.currentStreak < 7 -> "Building momentum. Solid."
                uiState.currentStreak < 14 -> "Two weeks in sight. You've got this."
                uiState.currentStreak < 30 -> "That's a real habit now."
                else -> "Impressive. This is who you are."
            },
            style = MaterialTheme.typography.bodyMedium.copy(color = NeutralGray),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Stat cards row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                label = "Best streak",
                value = "${uiState.bestStreak}",
                unit = "days"
            )
            StatCard(
                modifier = Modifier.weight(1f),
                label = "Completion rate",
                value = "${uiState.completionRate}",
                unit = "%"
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Weekly dots
        WeekRow(dots = uiState.weekDots)
    }
}

@Composable
private fun StreakCircle(streak: Int) {
    Box(
        modifier = Modifier
            .size(160.dp)
            .clip(CircleShape)
            .background(if (streak > 0) GreenPrimary else WarmGrayLight),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$streak",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 56.sp,
                    color = if (streak > 0) Color.White else NeutralGray
                )
            )
            Text(
                text = "days",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = if (streak > 0) Color.White.copy(alpha = 0.85f) else NeutralGray
                )
            )
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    unit: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderGray, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = NeutralGray,
                        fontWeight = FontWeight.Medium
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Charcoal
                        )
                    )
                    Text(
                        text = " $unit",
                        style = MaterialTheme.typography.bodyMedium.copy(color = NeutralGray),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun WeekRow(dots: List<Boolean>) {
    val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderGray, RoundedCornerShape(16.dp))
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Column {
                Text(
                    text = "This week",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = NeutralGray,
                        fontWeight = FontWeight.Medium
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    dayLabels.forEachIndexed { index, label ->
                        val complete = dots.getOrElse(index) { false }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (complete) GreenPrimary else WarmGrayLight),
                                contentAlignment = Alignment.Center
                            ) {
                                if (complete) {
                                    Text(
                                        text = "✓",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                            }
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = NeutralGray,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
