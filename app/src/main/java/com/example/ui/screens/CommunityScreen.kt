package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GuildSquad
import com.example.data.model.NobarChatMessage
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.ui.components.MoppiFrameAvatar
import com.example.ui.theme.*

@Composable
fun CommunityScreen(
    user: UserProfile,
    guilds: List<GuildSquad>,
    chatMessages: List<NobarChatMessage>,
    onSendMessage: (String) -> Unit,
    onJoinGuild: (String) -> Unit,
    onOpenGiftDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: 10. Public Chat, 1: 13. Guild/Squad
    var inputMessage by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MoppiSurfaceSubtle)
            .testTag("community_screen")
    ) {
        // Tab header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (selectedTab == 0) MoppiSapphire else MoppiWhite,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (selectedTab == 0) MoppiSapphire else MoppiBorder
                ),
                modifier = Modifier
                    .weight(1f)
                    .clickable { selectedTab = 0 }
            ) {
                Text(
                    text = "10. Chat Public",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    color = if (selectedTab == 0) Color.White else MoppiTextPrimary,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (selectedTab == 1) MoppiSapphire else MoppiWhite,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (selectedTab == 1) MoppiSapphire else MoppiBorder
                ),
                modifier = Modifier
                    .weight(1f)
                    .clickable { selectedTab = 1 }
            ) {
                Text(
                    text = "13. Guild / Squad (${guilds.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    color = if (selectedTab == 1) Color.White else MoppiTextPrimary,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
            }
        }

        if (selectedTab == 0) {
            // 10. Public Chat Tab
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 90.dp)
            ) {
                // Gift Gem/Premium banner action
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MoppiWhite,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MoppiBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable { onOpenGiftDialog() }
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.CardGiftcard,
                                contentDescription = "Gift",
                                tint = MoppiGemTeal,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "12. Give Gem / Kirim Hadiah Premium",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MoppiTextPrimary
                                )
                                Text(
                                    text = "Kirim Moppi Gem atau VIP ke teman wargamu",
                                    fontSize = 10.sp,
                                    color = MoppiTextTertiary
                                )
                            }
                        }

                        Button(
                            onClick = onOpenGiftDialog,
                            colors = ButtonDefaults.buttonColors(containerColor = MoppiGemTeal),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(text = "Kirim", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }

                // Chat Messages Feed
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    reverseLayout = true
                ) {
                    items(chatMessages) { msg ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MoppiWhite,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MoppiBorder),
                            shadowElevation = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                MoppiFrameAvatar(
                                    size = 36.dp,
                                    role = msg.senderRole,
                                    isPremium = msg.isSenderPremium,
                                    petEmoji = if (msg.senderRole == UserRole.DEVELOPER) "👑" else "🦊"
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = msg.senderName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = MoppiTextPrimary
                                        )

                                        // Role Badge
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = msg.senderRole.badgeColor.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = msg.senderRole.title,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 9.sp,
                                                color = msg.senderRole.badgeColor,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }

                                        Text(
                                            text = msg.timestamp,
                                            fontSize = 10.sp,
                                            color = MoppiTextTertiary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = msg.message,
                                        fontSize = 12.sp,
                                        color = MoppiTextSecondary,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Input Bar
                Surface(
                    color = MoppiWhite,
                    shadowElevation = 8.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MoppiBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputMessage,
                            onValueChange = { inputMessage = it },
                            placeholder = { Text("Tulis pesan publik...", fontSize = 12.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("community_chat_input"),
                            shape = RoundedCornerShape(20.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MoppiSurfaceSubtle,
                                unfocusedContainerColor = MoppiSurfaceSubtle,
                                focusedBorderColor = MoppiSapphire,
                                unfocusedBorderColor = MoppiBorder
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (inputMessage.isNotBlank()) {
                                    onSendMessage(inputMessage)
                                    inputMessage = ""
                                }
                            },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MoppiSapphire)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Send,
                                contentDescription = "Kirim",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        } else {
            // 13. Guild / Squad Tab
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Text(
                        text = "Anime Guild Leaderboard & Clan Wars",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MoppiTextPrimary
                    )
                }

                items(guilds) { guild ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MoppiWhite,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (guild.isJoined) MoppiSapphire else MoppiBorder
                        ),
                        shadowElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MoppiSurfaceSubtle)
                                    .border(1.dp, MoppiBorder, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "#${guild.rankPosition}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = when (guild.rankPosition) {
                                        1 -> MoppiGold
                                        2 -> Color(0xFF94A3B8)
                                        else -> MoppiSapphire
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = guild.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MoppiTextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MoppiViolet.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "[${guild.tag}]",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            color = MoppiViolet,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "\"${guild.slogan}\"",
                                    fontSize = 11.sp,
                                    color = MoppiTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "👥 ${guild.memberCount}/${guild.maxMembers} Anggota • 🏆 ${guild.trophyScore} Poin",
                                    fontSize = 10.sp,
                                    color = MoppiTextTertiary
                                )
                            }

                            Button(
                                onClick = { onJoinGuild(guild.id) },
                                enabled = !guild.isJoined,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (guild.isJoined) MoppiGemTeal else MoppiSurfaceMuted,
                                    disabledContainerColor = MoppiGemTeal
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (guild.isJoined) "Joined ✓" else "Join",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (guild.isJoined) Color.White else MoppiTextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
