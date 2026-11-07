package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.CoolFrame
import com.example.data.model.SupportedLanguage
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.ui.theme.*

@Composable
fun MoppiTopBar(
    user: UserProfile,
    equippedFrame: CoolFrame?,
    selectedLang: SupportedLanguage,
    onOpenGems: () -> Unit,
    onOpenLanguages: () -> Unit,
    onOpenAdmin: () -> Unit,
    onOpenProfile: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("moppi_top_bar"),
        color = MoppiWhite,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Brand Logo & Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onOpenProfile() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, MoppiBorder, RoundedCornerShape(10.dp))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.moppi_logo),
                            contentDescription = "MoppiTV Logo",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Moppi",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = MoppiTextPrimary
                            )
                            Text(
                                text = "TV",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = MoppiSapphire
                            )
                        }
                        Text(
                            text = "Next-Gen Anime Stream",
                            fontSize = 9.sp,
                            color = MoppiTextTertiary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Action controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Moppi Gems Chip
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MoppiSurfaceSubtle,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MoppiBorder),
                        modifier = Modifier
                            .clickable { onOpenGems() }
                            .testTag("top_bar_gems_chip")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Diamond,
                                contentDescription = "Moppi Gem",
                                tint = MoppiGemTeal,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${user.moppiGems}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MoppiTextPrimary
                            )
                        }
                    }

                    // 50 Languages Selector Button
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MoppiSurfaceSubtle,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MoppiBorder),
                        modifier = Modifier
                            .clickable { onOpenLanguages() }
                            .testTag("top_bar_lang_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = selectedLang.flag,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = selectedLang.code.uppercase(),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = MoppiTextSecondary
                            )
                        }
                    }

                    // Admin Dashboard quick action
                    if (user.role == UserRole.DEVELOPER || user.role == UserRole.ADMIN) {
                        IconButton(
                            onClick = onOpenAdmin,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MoppiSurfaceSubtle)
                                .testTag("top_bar_admin_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AdminPanelSettings,
                                contentDescription = "Panel Admin",
                                tint = if (user.role == UserRole.DEVELOPER) BadgeDeveloperRed else BadgeAdminPurple,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // User Avatar with Frame and Checklist Badge
                    MoppiFrameAvatar(
                        size = 36.dp,
                        frame = equippedFrame,
                        role = user.role,
                        isPremium = user.isPremium,
                        petEmoji = "🦊",
                        modifier = Modifier
                            .clickable { onOpenProfile() }
                            .testTag("top_bar_profile_avatar")
                    )
                }
            }
            HorizontalDivider(color = MoppiBorder, thickness = 1.dp)
        }
    }
}
