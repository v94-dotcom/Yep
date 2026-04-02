package com.yep.app.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.yep.app.ui.theme.GreenDarkest
import com.yep.app.ui.theme.GreenPrimary
import com.yep.app.ui.theme.Mint
import com.yep.app.ui.theme.NeutralGray

@Composable
fun OnboardingScreen(viewModel: OnboardingViewModel) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val selectedLabels by viewModel.selectedLabels.collectAsState()
    val customText by viewModel.customText.collectAsState()
    val reminderEnabled by viewModel.reminderEnabled.collectAsState()
    val reminderTime by viewModel.reminderTime.collectAsState()
    val focusManager = LocalFocusManager.current
    val isLastPage = pagerState.currentPage == 2

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> OnboardingPage1()
                1 -> OnboardingPage2()
                2 -> OnboardingPage3(
                    selectedLabels = selectedLabels,
                    customText = customText,
                    onToggle = viewModel::toggleSuggestion,
                    onCustomTextChange = viewModel::setCustomText,
                    reminderEnabled = reminderEnabled,
                    reminderTime = reminderTime,
                    onReminderEnabledChange = viewModel::setReminderEnabled,
                    onReminderTimeChange = viewModel::setReminderTime
                )
            }
        }

        // Skip button — top right, always visible
        TextButton(
            onClick = viewModel::completeOnboarding,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 8.dp)
        ) {
            Text(
                text = "Skip",
                style = MaterialTheme.typography.bodyMedium,
                color = NeutralGray
            )
        }

        // Fixed bottom bar: dots + swipe hint (pages 1-2) or Start button (page 3)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PageIndicator(
                pageCount = 3,
                currentPage = pagerState.currentPage
            )
            Spacer(modifier = Modifier.height(16.dp))
            BottomActionArea(
                isLastPage = isLastPage,
                onStart = {
                    focusManager.clearFocus()
                    viewModel.completeOnboarding()
                }
            )
        }
    }
}

// Extracted to its own composable so AnimatedVisibility resolves to the free-function
// overload rather than ColumnScope.AnimatedVisibility from an outer scope.
@Composable
private fun BottomActionArea(isLastPage: Boolean, onStart: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = !isLastPage,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Text(
                text = "Swipe to continue →",
                style = MaterialTheme.typography.bodySmall,
                color = NeutralGray
            )
        }
        AnimatedVisibility(
            visible = isLastPage,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Text(
                    text = "Start",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun OnboardingPage1() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 80.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "🔑", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "You know that feeling...",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Did I lock the door? Did I turn off the stove? That nagging doubt that follows you all day.",
            style = MaterialTheme.typography.bodyLarge,
            color = NeutralGray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun OnboardingPage2() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 80.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DemoItemCard()
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "One tap. Total peace of mind.",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Check off your daily items in seconds. No typing. No second-guessing.",
            style = MaterialTheme.typography.bodyLarge,
            color = NeutralGray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DemoItemCard() {
    val isDark = isSystemInDarkTheme()
    val cardBackground = if (isDark) MaterialTheme.colorScheme.surface else Mint
    val titleColor = if (isDark) MaterialTheme.colorScheme.primary else GreenDarkest
    val subtitleColor = if (isDark) MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)
                        else com.yep.app.ui.theme.GreenDark
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(cardBackground)
            .border(1.5.dp, GreenPrimary, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Locked the door",
                style = MaterialTheme.typography.titleMedium,
                color = titleColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Yep — just now",
                style = MaterialTheme.typography.bodySmall,
                color = subtitleColor
            )
        }
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(GreenPrimary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
private fun OnboardingPage3(
    selectedLabels: Set<String>,
    customText: String,
    onToggle: (String) -> Unit,
    onCustomTextChange: (String) -> Unit,
    reminderEnabled: Boolean,
    reminderTime: String,
    onReminderEnabledChange: (Boolean) -> Unit,
    onReminderTimeChange: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    var showTimePicker by remember { mutableStateOf(false) }

    val notifPermission = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    // On API < 33 the permission doesn't exist — treat as already granted
    val notifGranted = notifPermission.status.isGranted || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
    // When permission arrives after launchPermissionRequest(), enable the reminder
    var pendingEnable by remember { mutableStateOf(false) }
    LaunchedEffect(notifPermission.status) {
        if (pendingEnable && notifPermission.status.isGranted) {
            onReminderEnabledChange(true)
            pendingEnable = false
        }
    }

    if (showTimePicker) {
        val parts = reminderTime.split(":")
        val timePickerState = rememberTimePickerState(
            initialHour = parts[0].toInt(),
            initialMinute = parts[1].toInt(),
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val newTime = "%02d:%02d".format(timePickerState.hour, timePickerState.minute)
                    onReminderTimeChange(newTime)
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
            text = { TimePicker(state = timePickerState) }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 140.dp) // clear the fixed bottom bar
    ) {
        Column(
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 64.dp, bottom = 16.dp)
        ) {
            Text(
                text = "What do you check\nevery day?",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Pick the ones that apply. You can always add more later.",
                style = MaterialTheme.typography.bodyMedium,
                color = NeutralGray
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(OnboardingViewModel.SUGGESTIONS) { label ->
                SuggestionRow(
                    label = label,
                    checked = label in selectedLabels,
                    onToggle = { onToggle(label) }
                )
            }
        }

        OutlinedTextField(
            value = customText,
            onValueChange = onCustomTextChange,
            placeholder = { Text("Add your own…", color = NeutralGray) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
        )

        HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Gentle reminder?",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = reminderEnabled,
                onCheckedChange = { enabled ->
                    if (!enabled) {
                        onReminderEnabledChange(false)
                    } else if (notifGranted) {
                        onReminderEnabledChange(true)
                    } else {
                        pendingEnable = true
                        notifPermission.launchPermissionRequest()
                    }
                },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = GreenPrimary)
            )
        }

        AnimatedVisibility(visible = reminderEnabled) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { showTimePicker = true }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint = GreenPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Remind me at",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = reminderTime,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = GreenPrimary
                )
            }
        }
    }
}

@Composable
private fun SuggestionRow(label: String, checked: Boolean, onToggle: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val cardBackground = if (checked && !isDark) Mint else MaterialTheme.colorScheme.surface
    val borderColor = if (checked) GreenPrimary else MaterialTheme.colorScheme.outline
    val textColor = when {
        checked && !isDark -> GreenDarkest
        checked -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(cardBackground)
            .border(
                width = if (checked) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onToggle)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (checked) GreenPrimary else Color.Transparent)
                .border(
                    width = if (checked) 0.dp else 1.5.dp,
                    color = if (checked) Color.Transparent else MaterialTheme.colorScheme.outline,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun PageIndicator(pageCount: Int, currentPage: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val width by animateDpAsState(
                targetValue = if (index == currentPage) 20.dp else 6.dp,
                label = "indicator_width"
            )
            Box(
                modifier = Modifier
                    .height(6.dp)
                    .width(width)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (index == currentPage) GreenPrimary else MaterialTheme.colorScheme.outline)
            )
        }
    }
}
