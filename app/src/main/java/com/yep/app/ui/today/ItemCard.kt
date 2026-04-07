package com.yep.app.ui.today

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.yep.app.data.entities.Confirmation
import com.yep.app.data.entities.Item
import com.yep.app.ui.theme.GreenPrimary
import com.yep.app.ui.theme.NeutralGray
import com.yep.app.util.DateUtils

@Composable
fun ItemCard(
    item: Item,
    confirmation: Confirmation? = null,
    isExpanded: Boolean = false,
    isEditMode: Boolean = false,
    dragHandleModifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onConfirm: () -> Unit = {},
    onConfirmWithPhoto: () -> Unit = {},
    onDelete: () -> Unit = {},
    onUncheck: () -> Unit = {},
    onViewPhoto: () -> Unit = {}
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isConfirmed = confirmation != null
    val hasPhoto = !confirmation?.photoPaths.isNullOrEmpty()
    val photoCount = confirmation?.photoPaths?.size ?: 0
    val cardShape = RoundedCornerShape(16.dp)

    val checkScale by animateFloatAsState(
        targetValue = if (isConfirmed) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "checkScale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isConfirmed) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        animationSpec = tween(200),
        label = "cardBg"
    )

    val confirmedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
    val confirmedSubtextColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
    val outlineColor = MaterialTheme.colorScheme.outline

    val stateDesc = when {
        isConfirmed -> "Confirmed at ${DateUtils.formatTime(context, confirmation!!.confirmedAt)}"
        isExpanded -> "Expanded, choose action"
        else -> "Not yet confirmed"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(backgroundColor)
            .then(
                when {
                    isExpanded && !isConfirmed -> Modifier.border(2.dp, GreenPrimary, cardShape)
                    !isConfirmed -> Modifier.dashedBorder(outlineColor, 16.dp)
                    else -> Modifier
                }
            )
            .clickable(enabled = (!isConfirmed || hasPhoto) && !isEditMode) {
                if (hasPhoto) onViewPhoto() else onClick()
            }
            .semantics { stateDescription = stateDesc }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = if (isEditMode) 12.dp else 16.dp,
                    end = 16.dp,
                    top = 14.dp,
                    bottom = 14.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isEditMode) {
                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = "Drag to reorder",
                    tint = NeutralGray.copy(alpha = 0.45f),
                    modifier = Modifier
                        .size(20.dp)
                        .then(dragHandleModifier)
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isConfirmed) confirmedTextColor else MaterialTheme.colorScheme.onSurface
                )
                if (isConfirmed) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Yep — ${DateUtils.formatTime(context, confirmation!!.confirmedAt)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = confirmedSubtextColor
                        )
                        if (hasPhoto) {
                            Surface(
                                shape = RoundedCornerShape(99.dp),
                                color = GreenPrimary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (photoCount == 1) "photo" else "$photoCount photos",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = confirmedSubtextColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Not yet today",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeutralGray
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            if (isEditMode) {
                if (isConfirmed) {
                    IconButton(onClick = onUncheck, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Uncheck",
                            tint = NeutralGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete item",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else if (isConfirmed) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .scale(checkScale)
                        .clip(CircleShape)
                        .background(GreenPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Confirmed",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .border(
                            1.5.dp,
                            if (isExpanded) GreenPrimary else outlineColor,
                            CircleShape
                        )
                )
            }
        }

        AnimatedVisibility(
            visible = isExpanded && !isConfirmed && !isEditMode,
            enter = expandVertically(animationSpec = tween(150)),
            exit = shrinkVertically(animationSpec = tween(150))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onConfirm()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Yep", style = MaterialTheme.typography.labelLarge)
                }
                Button(
                    onClick = onConfirmWithPhoto,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Yep + photo", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

private fun Modifier.dashedBorder(color: Color, cornerRadius: Dp): Modifier =
    this.drawBehind {
        drawRoundRect(
            color = color,
            cornerRadius = CornerRadius(cornerRadius.toPx()),
            style = Stroke(
                width = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
            )
        )
    }
