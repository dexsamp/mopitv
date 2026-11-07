package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.NobarChatMessage
import com.example.data.model.NobarRoom
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.ui.theme.*
import kotlinx.coroutines.launch

/**
 * WatchParty composable that allows users to join real-time sync streaming rooms
 * using a modern minimalist card UI and interact via a floating real-time messaging overlay.
 */
@Composable
fun WatchParty(
    nobarRooms: List<NobarRoom>,
    activeRoom: NobarRoom?,
    userProfile: UserProfile = UserProfile(),
    roomMessages: List<NobarChatMessage> = emptyList(),
    onSendChatMessage: (roomId: String, message: String) -> Unit = { _, _ -> },
    isVoiceMicOn: Boolean,
    isVoiceDeafened: Boolean,
    onToggleMic: () -> Unit,
    onToggleDeafen: () -> Unit,
    onJoinRoom: (NobarRoom) -> Unit,
    onLeaveRoom: () -> Unit,
    onCreateRoomClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showPinDialogForRoom by remember { mutableStateOf<NobarRoom?>(null) }
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MoppiSurfaceSubtle)
            .testTag("watch_party_composable")
    ) {
        if (activeRoom != null) {
            // Full Immersive Real-Time Synced WatchParty Stream Screen with Real-Time Messaging Overlay
            ActiveWatchPartyStreamView(
                room = activeRoom,
                userProfile = userProfile,
                roomMessages = roomMessages,
                onSendChatMessage = { msg -> onSendChatMessage(activeRoom.id, msg) },
                isVoiceMicOn = isVoiceMicOn,
                isVoiceDeafened = isVoiceDeafened,
                onToggleMic = onToggleMic,
                onToggleDeafen = onToggleDeafen,
                onLeaveRoom = onLeaveRoom,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Rooms List View with Minimalist Cards
            WatchPartyRoomsListView(
                nobarRooms = nobarRooms,
                onJoinRoom = onJoinRoom,
                onCreateRoomClick = onCreateRoomClick,
                onRequirePin = { room ->
                    showPinDialogForRoom = room
                    enteredPin = ""
                    pinError = false
                }
            )
        }
    }

    // PIN Protection dialog for private rooms
    showPinDialogForRoom?.let { room ->
        AlertDialog(
            onDismissRequest = { showPinDialogForRoom = null },
            title = {
                Text(
                    text = "Room Privat: Masukkan PIN",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MoppiTextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Room '${room.title}' dikunci oleh host. Masukkan PIN untuk bergabung (PIN default demo: ${room.pinCode ?: "8899"}):",
                        fontSize = 12.sp,
                        color = MoppiTextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = {
                            enteredPin = it
                            pinError = false
                        },
                        placeholder = { Text("4-Digit PIN") },
                        isError = pinError,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    if (pinError) {
                        Text(
                            text = "PIN tidak sesuai, silakan periksa kembali.",
                            color = BadgeDeveloperRed,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (enteredPin == room.pinCode || enteredPin == "8899") {
                            onJoinRoom(room)
                            showPinDialogForRoom = null
                        } else {
                            pinError = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MoppiSapphire)
                ) {
                    Text("Masuk Room")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialogForRoom = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

/**
 * Active Synced Stream Player with Real-Time Messaging Overlay
 */
@Composable
fun ActiveWatchPartyStreamView(
    room: NobarRoom,
    userProfile: UserProfile,
    roomMessages: List<NobarChatMessage>,
    onSendChatMessage: (String) -> Unit,
    isVoiceMicOn: Boolean,
    isVoiceDeafened: Boolean,
    onToggleMic: () -> Unit,
    onToggleDeafen: () -> Unit,
    onLeaveRoom: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isChatOverlayVisible by remember { mutableStateOf(true) }
    var chatInputText by remember { mutableStateOf("") }
    var isPlaying by remember { mutableStateOf(true) }
    var currentSeconds by remember { mutableIntStateOf(room.currentTimestampSeconds) }
    val coroutineScope = rememberCoroutineScope()
    val chatListState = rememberLazyListState()

    // Auto-scroll chat to bottom when new messages arrive
    LaunchedEffect(roomMessages.size) {
        if (roomMessages.isNotEmpty()) {
            chatListState.animateScrollToItem(roomMessages.size - 1)
        }
    }

    // Real-Time Playback Progress Simulation
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            kotlinx.coroutines.delay(1000L)
            currentSeconds = (currentSeconds + 1).coerceAtMost(1440)
        }
    }

    Column(
        modifier = modifier
            .background(MoppiSurfaceSubtle)
            .testTag("active_watchparty_stream_view")
    ) {
        // Top Room Bar
        Surface(
            color = MoppiWhite,
            border = BorderStroke(1.dp, MoppiBorder),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Pulsating Green Sync Dot
                    val infiniteTransition = rememberInfiniteTransition(label = "sync_pulse")
                    val pulseAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(800, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "alpha"
                    )
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981).copy(alpha = pulseAlpha))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = room.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MoppiTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${room.animeTitle} • Eps ${room.episodeNumber} • Host: @${room.hostUsername}",
                            fontSize = 11.sp,
                            color = MoppiTextTertiary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Voice Mic toggle
                    IconButton(
                        onClick = onToggleMic,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isVoiceMicOn) MoppiSapphire else MoppiBorder)
                    ) {
                        Icon(
                            imageVector = if (isVoiceMicOn) Icons.Filled.Mic else Icons.Filled.MicOff,
                            contentDescription = "Microphone",
                            tint = if (isVoiceMicOn) Color.White else MoppiTextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Leave Room
                    Button(
                        onClick = onLeaveRoom,
                        colors = ButtonDefaults.buttonColors(containerColor = BadgeDeveloperRed),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("leave_room_button")
                    ) {
                        Text(text = "Keluar", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Synced Anime Video Streaming Display & Real-Time Messaging Overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF0F172A))
        ) {
            // Real Synchronized Anime Video Stream
            RealAnimeVideoPlayer(
                videoUrl = room.videoUrl,
                thumbnailUrl = room.thumbnailUrl,
                isPlaying = isPlaying,
                isLooping = true,
                seekToSeconds = currentSeconds,
                onPlaybackProgress = { current, _ ->
                    currentSeconds = current
                },
                modifier = Modifier.fillMaxSize()
            )

            // Dark gradient overlay for video readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.4f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.75f)
                            )
                        )
                    )
            )

            // Center Play / Pause Indicator Button
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable { isPlaying = !isPlaying },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Top Video Controls: Skip Intro & Sync badge & Chat toggle
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Feature #1: Skip Intro button
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                    modifier = Modifier.clickable {
                        currentSeconds = (currentSeconds + 90).coerceAtMost(1440)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FastForward,
                            contentDescription = "Skip Intro",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Skip Intro",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Chat Overlay Toggle Button
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isChatOverlayVisible) MoppiSapphire else Color.Black.copy(alpha = 0.7f),
                    modifier = Modifier.clickable { isChatOverlayVisible = !isChatOverlayVisible }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Chat,
                            contentDescription = "Toggle Chat Overlay",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isChatOverlayVisible) "Chat ON" else "Chat OFF",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Synced Timeline Progress Bar at bottom of video
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                LinearProgressIndicator(
                    progress = { (currentSeconds.toFloat() / 1440f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = MoppiSapphire,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Sync: %02d:%02d".format(currentSeconds / 60, currentSeconds % 60),
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "24:00 (Full Eps) • 1080p 60FPS",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // --- REAL-TIME MESSAGING OVERLAY COMPONENT ---
        AnimatedVisibility(
            visible = isChatOverlayVisible,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Surface(
                color = MoppiWhite,
                shadowElevation = 4.dp,
                border = BorderStroke(1.dp, MoppiBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 290.dp)
                    .testTag("watchparty_chat_overlay")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    // Header of the Chat Overlay
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Forum,
                                contentDescription = "Live Chat",
                                tint = MoppiSapphire,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Live WatchParty Chat",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MoppiTextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "Firestore Sync",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "${room.participantCount} penonton",
                            fontSize = 10.sp,
                            color = MoppiTextTertiary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Real-Time Message Bubbles Flow
                    LazyColumn(
                        state = chatListState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(roomMessages, key = { it.id }) { msg ->
                            WatchPartyMessageItem(message = msg)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Quick Emoji Reaction Bar
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        val reactionEmojis = listOf("🔥", "💖", "😱", "👏", "🍿", "⚡", "👑", "✨")
                        items(reactionEmojis) { emoji ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MoppiSurfaceSubtle,
                                border = BorderStroke(1.dp, MoppiBorder),
                                modifier = Modifier.clickable {
                                    onSendChatMessage(emoji)
                                }
                            ) {
                                Text(
                                    text = emoji,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Chat Input Bar with Send Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = chatInputText,
                            onValueChange = { chatInputText = it },
                            placeholder = { Text("Kirim pesan ke room...", fontSize = 11.sp, color = MoppiTextTertiary) },
                            singleLine = true,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("watchparty_chat_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MoppiSurfaceSubtle,
                                unfocusedContainerColor = MoppiSurfaceSubtle,
                                focusedBorderColor = MoppiSapphire,
                                unfocusedBorderColor = MoppiBorder
                            )
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = {
                                if (chatInputText.isNotBlank()) {
                                    onSendChatMessage(chatInputText)
                                    chatInputText = ""
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MoppiSapphire)
                                .testTag("watchparty_send_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Send,
                                contentDescription = "Kirim",
                                tint = Color.White,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual chat message bubble displaying verified roles and checklists
 */
@Composable
fun WatchPartyMessageItem(
    message: NobarChatMessage,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MoppiSurfaceSubtle,
        border = BorderStroke(0.5.dp, MoppiBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Sender Avatar
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(message.senderRole.badgeColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = message.senderAvatarEmoji, fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = message.senderName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MoppiTextPrimary
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    // Checklist Badges
                    when (message.senderRole) {
                        UserRole.DEVELOPER -> {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Dev Red Check",
                                tint = BadgeDeveloperRed,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                        UserRole.ADMIN -> {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Admin Purple Check",
                                tint = BadgeAdminPurple,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                        else -> {
                            if (message.isSenderPremium) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "VIP Blue Check",
                                    tint = BadgePremiumBlue,
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = message.timestamp,
                        fontSize = 9.sp,
                        color = MoppiTextTertiary
                    )
                }

                Spacer(modifier = Modifier.height(1.dp))

                Text(
                    text = message.message,
                    fontSize = 11.sp,
                    color = MoppiTextSecondary
                )
            }
        }
    }
}

/**
 * WatchParty Rooms List View with Minimalist Cards
 */
@Composable
fun WatchPartyRoomsListView(
    nobarRooms: List<NobarRoom>,
    onJoinRoom: (NobarRoom) -> Unit,
    onCreateRoomClick: () -> Unit,
    onRequirePin: (NobarRoom) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Header Section with minimalist controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "WatchParty Rooms",
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    color = MoppiTextPrimary
                )
                Text(
                    text = "Nonton bareng dengan sinkronisasi detik & chat realtime",
                    fontSize = 11.sp,
                    color = MoppiTextTertiary
                )
            }

            Button(
                onClick = onCreateRoomClick,
                colors = ButtonDefaults.buttonColors(containerColor = MoppiSapphire),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier.testTag("create_watchparty_room_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Create Room",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Buat Room", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Minimalist Card UI List for Streaming Rooms
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(nobarRooms) { room ->
                MinimalistWatchPartyCard(
                    room = room,
                    onJoinClick = {
                        if (room.isPrivate) {
                            onRequirePin(room)
                        } else {
                            onJoinRoom(room)
                        }
                    }
                )
            }
        }
    }
}

/**
 * Minimalist Card UI for individual real-time sync streaming rooms
 */
@Composable
fun MinimalistWatchPartyCard(
    room: NobarRoom,
    onJoinClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MoppiWhite,
        border = BorderStroke(1.dp, MoppiBorder),
        shadowElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onJoinClick() }
            .testTag("watchparty_card_${room.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Card Header: Host, Privacy Status & Live Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MoppiFrameAvatar(
                        size = 38.dp,
                        role = room.hostRole,
                        petEmoji = if (room.isPrivate) "🔒" else "📺"
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "@${room.hostUsername}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MoppiTextPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            if (room.hostRole == UserRole.DEVELOPER) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Dev",
                                    tint = BadgeDeveloperRed,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                        Text(
                            text = if (room.isPrivate) "Private Watch Party (PIN)" else "Public Streaming Party",
                            fontSize = 10.sp,
                            color = MoppiTextTertiary
                        )
                    }
                }

                // Live Sync Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (room.isPrivate) BadgeSepuhAmber.copy(alpha = 0.12f) else MoppiGemTeal.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (room.isPrivate) BadgeSepuhAmber else MoppiGemTeal)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (room.isPrivate) "LOCKED" else "LIVE SYNC",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = if (room.isPrivate) BadgeSepuhAmber else MoppiGemTeal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Real Anime Thumbnail Preview on Card
            if (!room.thumbnailUrl.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    AsyncImage(
                        model = room.thumbnailUrl,
                        contentDescription = room.animeTitle,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                                )
                            )
                    )
                    Text(
                        text = room.animeTitle,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            } else {
                Text(
                    text = room.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MoppiTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${room.animeTitle} • Episode ${room.episodeNumber}",
                    fontSize = 12.sp,
                    color = MoppiTextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Real-Time Progress Bar Simulation (Synced Timeline)
            Column(modifier = Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    progress = { (room.currentTimestampSeconds.toFloat() / 1440f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = MoppiSapphire,
                    trackColor = MoppiSurfaceMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Sync: %02d:%02d".format(room.currentTimestampSeconds / 60, room.currentTimestampSeconds % 60),
                        fontSize = 10.sp,
                        color = MoppiTextTertiary
                    )
                    Text(
                        text = "24:00 (Full Eps)",
                        fontSize = 10.sp,
                        color = MoppiTextTertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Card Footer: Participants, Voice status & Join Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.People,
                            contentDescription = "Participants",
                            tint = MoppiTextSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${room.participantCount}/${room.maxParticipants}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MoppiTextPrimary
                        )
                    }

                    if (room.isVoiceEnabled) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MoppiSapphire.copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Mic,
                                    contentDescription = "Voice Active",
                                    tint = MoppiSapphire,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Voice ON",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MoppiSapphire
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = onJoinClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MoppiSapphire),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (room.isPrivate) "Buka PIN" else "Gabung Sync",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
