package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.MoppiFrameAvatar
import com.example.ui.theme.*

@Composable
fun ProfileScreen(
    user: UserProfile,
    equippedFrame: CoolFrame?,
    equippedPet: AnimePet?,
    achievements: List<Achievement>,
    onClaimAchievement: (String) -> Unit,
    onSwitchRole: (UserRole) -> Unit,
    onTogglePremium: (Boolean) -> Unit,
    onChangeBackground: (ProfileBackgroundType) -> Unit,
    onOpenAdminPanel: () -> Unit,
    onOpenLanguages: () -> Unit,
    onOpenGiftDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 100.dp),
        modifier = modifier
            .fillMaxSize()
            .background(MoppiSurfaceSubtle)
            .testTag("profile_screen")
    ) {
        // 26. CUSTOM BACKGROUND PROFILE GIF / GRADIENT
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Brush.linearGradient(user.backgroundGifType.gradient))
            ) {
                // Background Selector Floating Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.5f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Wallpaper,
                            contentDescription = "Background",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "26. Background GIF",
                            fontSize = 10.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Profile Avatar & Card Header (Overlapping background)
        item {
            Surface(
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = MoppiWhite,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-24).dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar with frame and checklist
                    MoppiFrameAvatar(
                        size = 76.dp,
                        frame = equippedFrame,
                        role = user.role,
                        isPremium = user.isPremium,
                        petEmoji = equippedPet?.iconEmoji ?: "🦊",
                        modifier = Modifier.offset(y = (-36).dp)
                    )

                    Spacer(modifier = Modifier.height((-20).dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = user.username,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = MoppiTextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))

                        // 27. Checklist merah untuk developer
                        // 28. Checklist biru untuk prem dan ungu untuk moderator/admin
                        when {
                            user.role == UserRole.DEVELOPER -> {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Dev Red Checklist",
                                    tint = BadgeDeveloperRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            user.role == UserRole.ADMIN -> {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Admin Purple Checklist",
                                    tint = BadgeAdminPurple,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            user.isPremium -> {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Premium Blue Checklist",
                                    tint = BadgePremiumBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            else -> Unit
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Guild: ${user.guildName ?: "Solo Wanderer"} • ${user.episodesWatched} Eps Ditonton",
                        fontSize = 12.sp,
                        color = MoppiTextSecondary
                    )

                    // 18. SYSTEM WARN BANNER (If user has warnings)
                    if (user.warningsCount > 0) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = BadgeDeveloperRed.copy(alpha = 0.1f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BadgeDeveloperRed)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Warning,
                                    contentDescription = "System Warn",
                                    tint = BadgeDeveloperRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "18. System Warn: ${user.warningsCount} Peringatan Aktif! (${user.warningReason ?: "Patuhilah aturan komunitas"})",
                                    fontSize = 11.sp,
                                    color = BadgeDeveloperRed,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Stats Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MoppiSurfaceSubtle)
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${user.moppiGems}",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = MoppiGemTeal
                            )
                            Text(text = "9. Moppi Gem", fontSize = 10.sp, color = MoppiTextTertiary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = equippedPet?.name?.take(8) ?: "None",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = equippedPet?.rarity?.color ?: MoppiSapphire
                            )
                            Text(text = "2. Active Pet", fontSize = 10.sp, color = MoppiTextTertiary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (user.isPremium) "VIP ACTIVE" else "FREE",
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                color = if (user.isPremium) BadgePremiumBlue else MoppiTextPrimary
                            )
                            Text(text = "8. Status Prem", fontSize = 10.sp, color = MoppiTextTertiary)
                        }
                    }
                }
            }
        }

        // 21. ROLE SYSTEM SWITCHER (admin, developer, warga, sepuh)
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MoppiWhite,
                border = androidx.compose.foundation.BorderStroke(1.dp, MoppiBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "21. Role System (Uji Coba Hak Akses & Badge)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MoppiTextPrimary
                    )
                    Text(
                        text = "Pilih peran untuk menguji tampilan checklist merah, biru, dan ungu:",
                        fontSize = 11.sp,
                        color = MoppiTextTertiary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(UserRole.values()) { role ->
                            val isCurrent = user.role == role
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isCurrent) role.badgeColor else MoppiSurfaceSubtle,
                                border = androidx.compose.foundation.BorderStroke(1.dp, role.badgeColor),
                                modifier = Modifier.clickable { onSwitchRole(role) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = role.title,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCurrent) Color.White else role.badgeColor
                                    )
                                    if (role.hasRedChecklist) {
                                        Text(text = " (Red ✓)", fontSize = 9.sp, color = if (isCurrent) Color.White else BadgeDeveloperRed)
                                    }
                                    if (role.hasPurpleChecklist) {
                                        Text(text = " (Purple ✓)", fontSize = 9.sp, color = if (isCurrent) Color.White else BadgeAdminPurple)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    // 8. Toggle Premium VIP
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "8. Status Premium (Checklist Biru):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MoppiTextPrimary
                        )
                        Switch(
                            checked = user.isPremium,
                            onCheckedChange = onTogglePremium,
                            colors = SwitchDefaults.colors(checkedThumbColor = BadgePremiumBlue)
                        )
                    }
                }
            }
        }

        // 26. Background GIF Selector Carousel
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MoppiWhite,
                border = androidx.compose.foundation.BorderStroke(1.dp, MoppiBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "26. Custom Background Profile",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MoppiTextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(ProfileBackgroundType.values()) { bg ->
                            val isSel = user.backgroundGifType == bg
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MoppiWhite,
                                border = androidx.compose.foundation.BorderStroke(
                                    2.dp,
                                    if (isSel) MoppiSapphire else MoppiBorder
                                ),
                                modifier = Modifier
                                    .width(130.dp)
                                    .clickable { onChangeBackground(bg) }
                            ) {
                                Column {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                            .background(Brush.linearGradient(bg.gradient))
                                    )
                                    Text(
                                        text = bg.label,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) MoppiSapphire else MoppiTextPrimary,
                                        modifier = Modifier.padding(6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. ACHIEVEMENTS REWARD SECTION
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MoppiWhite,
                border = androidx.compose.foundation.BorderStroke(1.dp, MoppiBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "3. Achievement Rewards (Klaim Moppi Gem)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MoppiTextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    achievements.forEach { ach ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ach.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MoppiTextPrimary
                                )
                                Text(
                                    text = ach.description,
                                    fontSize = 10.sp,
                                    color = MoppiTextSecondary
                                )
                                Text(
                                    text = "Progress: ${ach.currentProgress}/${ach.targetProgress}",
                                    fontSize = 10.sp,
                                    color = MoppiTextTertiary
                                )
                            }

                            Button(
                                onClick = { onClaimAchievement(ach.id) },
                                enabled = !ach.isClaimed && ach.currentProgress >= ach.targetProgress,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MoppiGemTeal,
                                    disabledContainerColor = MoppiSurfaceMuted
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (ach.isClaimed) "Diklaim ✓" else "+${ach.gemReward} 💎",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (ach.isClaimed) MoppiTextTertiary else Color.White
                                )
                            }
                        }
                        HorizontalDivider(color = MoppiBorder, thickness = 0.5.dp)
                    }
                }
            }
        }

        // Quick Settings Actions (19. Admin Panel, 6. 50 Bahasa)
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MoppiWhite,
                border = androidx.compose.foundation.BorderStroke(1.dp, MoppiBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Admin Panel Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenAdminPanel() }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AdminPanelSettings,
                            contentDescription = "Admin Panel",
                            tint = BadgeDeveloperRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "19. System Panel Admin & Moderasi",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MoppiTextPrimary
                            )
                            Text(
                                text = "Kelola pengumuman darurat, system warn, dan broadcast",
                                fontSize = 10.sp,
                                color = MoppiTextTertiary
                            )
                        }
                    }

                    HorizontalDivider(color = MoppiBorder, thickness = 0.5.dp)

                    // 6. 50 Languages Modal trigger
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenLanguages() }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Language,
                            contentDescription = "Language",
                            tint = MoppiSapphire,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "6. Pilihan 50 Bahasa Subtitle & Audio",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MoppiTextPrimary
                            )
                            Text(
                                text = "Ganti bahasa aplikasi dan subtitle anime",
                                fontSize = 10.sp,
                                color = MoppiTextTertiary
                            )
                        }
                    }
                }
            }
        }
    }
}
