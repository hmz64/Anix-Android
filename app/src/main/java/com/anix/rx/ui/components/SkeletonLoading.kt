package com.anix.rx.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SkeletonLoading(
    modifier: Modifier = Modifier,
    width: androidx.compose.ui.unit.Dp = 200.dp,
    height: androidx.compose.ui.unit.Dp = 100.dp
) {
    val transition = rememberInfiniteTransition(label = "skeleton_transition")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeleton_alpha"
    )
    
    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .drawBehind {
                drawRoundRect(
                    color = Color.Gray.copy(alpha = alpha),
                    cornerRadius = CornerRadius(8.dp.toPx())
                )
            }
    )
}

@Composable
fun AnimeCardSkeleton(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SkeletonLoading(
            width = 160.dp,
            height = 220.dp
        )
        Spacer(modifier = Modifier.height(8.dp))
        SkeletonLoading(
            width = 120.dp,
            height = 16.dp
        )
        Spacer(modifier = Modifier.height(4.dp))
        SkeletonLoading(
            width = 80.dp,
            height = 12.dp
        ) // Tadi ada karakter ` di sini, sudah saya hapus
    } // Kurung penutup ini tadi hilang, sekarang sudah ada
}
