package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CoolFrame
import com.example.data.model.UserRole
import com.example.ui.theme.*

@Composable
fun MoppiFrameAvatar(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    frame: CoolFrame? = null,
    role: UserRole = UserRole.WARGA,
    isPremium: Boolean = false,
    initials: String = "M",
    petEmoji: String? = "🦊"
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Frame Border (Cool Bingkai)
        val borderBrush = if (frame != null && frame.gradientColors.isNotEmpty()) {
            Brush.sweepGradient(frame.gradientColors)
        } else {
            Brush.linearGradient(listOf(MoppiBorder, MoppiBorderHighlight))
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(borderBrush)
                .padding(if (frame != null) 3.dp else 1.5.dp)
                .clip(CircleShape)
                .background(MoppiSurfaceWhite),
            contentAlignment = Alignment.Center
        ) {
            // Inner Avatar
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            listOf(MoppiSurfaceSubtle, MoppiSurfaceMuted)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (petEmoji != null) {
                    Text(
                        text = petEmoji,
                        fontSize = (size.value * 0.45f).sp
                    )
                } else {
                    Text(
                        text = initials,
                        fontWeight = FontWeight.Bold,
                        color = MoppiTextPrimary,
                        fontSize = (size.value * 0.4f).sp
                    )
                }
            }
        }

        // Verification Badge Checkmarks:
        // 27. cheklist merah untuk developer
        // 28. cheklist biru untuk prem dan ungu untuk moderator/admin
        val badgeConfig = when {
            role == UserRole.DEVELOPER -> Pair(BadgeDeveloperRed, "Dev")
            role == UserRole.ADMIN -> Pair(BadgeAdminPurple, "Mod")
            isPremium -> Pair(BadgePremiumBlue, "VIP")
            role == UserRole.SEPUH -> Pair(BadgeSepuhAmber, "Old")
            else -> null
        }

        if (badgeConfig != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 2.dp, y = 2.dp)
                    .size((size.value * 0.38f).coerceAtLeast(16f).dp)
                    .clip(CircleShape)
                    .background(MoppiWhite)
                    .padding(1.dp)
                    .clip(CircleShape)
                    .background(badgeConfig.first),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Verified ${badgeConfig.second}",
                    tint = MoppiWhite,
                    modifier = Modifier.fillMaxSize(0.85f)
                )
            }
        }
    }
}
