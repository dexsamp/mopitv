package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NobarRoom
import com.example.data.model.UserRole
import com.example.ui.components.MoppiFrameAvatar
import com.example.ui.theme.*

@Composable
fun NobarScreen(
    nobarRooms: List<NobarRoom>,
    activeRoom: NobarRoom?,
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
            .testTag("nobar_screen")
    ) {
        // Active Watch Party HUD if inside a room
        if (activeRoom != null) {
            Surface(
                color = MoppiWhite,
                shadowElevation = 4.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, MoppiBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SEDANG NOBAR LIVE SYNC",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF10B981)
                            )
                        }

                        Button(
                            onClick = onLeaveRoom,
                            colors = ButtonDefaults.buttonColors(containerColor = BadgeDeveloperRed),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(text = "Keluar Room", fontSize = 11.sp, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = activeRoom.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MoppiTextPrimary
                    )
                    Text(
                        text = "${activeRoom.animeTitle} • Eps ${activeRoom.episodeNumber}",
                        fontSize = 12.sp,
                        color = MoppiTextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 4. VOICE PUBLIC AND PRIVATE CONTROLS
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MoppiSurfaceSubtle,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MoppiBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (activeRoom.isPrivate) Icons.Filled.Lock else Icons.Filled.RecordVoiceOver,
                                    contentDescription = "Voice Channel",
                                    tint = MoppiSapphire,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = if (activeRoom.isPrivate) "Private Voice Channel" else "Public Voice Room",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MoppiTextPrimary
                                    )
                                    Text(
                                        text = "${activeRoom.participantCount} anggota terhubung",
                                        fontSize = 10.sp,
                                        color = MoppiTextTertiary
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(
                                    onClick = onToggleMic,
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(if (isVoiceMicOn) MoppiSapphire else MoppiBorder)
                                ) {
                                    Icon(
                                        imageVector = if (isVoiceMicOn) Icons.Filled.Mic else Icons.Filled.MicOff,
                                        contentDescription = "Mic",
                                        tint = if (isVoiceMicOn) Color.White else MoppiTextPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                IconButton(
                                    onClick = onToggleDeafen,
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(if (!isVoiceDeafened) MoppiSurfaceMuted else BadgeDeveloperRed)
                                ) {
                                    Icon(
                                        imageVector = if (isVoiceDeafened) Icons.Filled.HeadsetOff else Icons.Filled.Headphones,
                                        contentDescription = "Deafen",
                                        tint = if (isVoiceDeafened) Color.White else MoppiTextPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Header and Create Room Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "5. Room Nobar (Watch Party)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MoppiTextPrimary
                )
                Text(
                    text = "Nonton bareng anime tersinkron & voice chat",
                    fontSize = 11.sp,
                    color = MoppiTextTertiary
                )
            }

            Button(
                onClick = onCreateRoomClick,
                colors = ButtonDefaults.buttonColors(containerColor = MoppiSapphire),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Buat Room",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Buat Room", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Nobar Rooms List
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(nobarRooms) { room ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MoppiWhite,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MoppiBorder),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (room.isPrivate) {
                                showPinDialogForRoom = room
                                enteredPin = ""
                                pinError = false
                            } else {
                                onJoinRoom(room)
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MoppiFrameAvatar(
                            size = 44.dp,
                            role = room.hostRole,
                            petEmoji = if (room.isPrivate) "🔒" else "📺"
                        )
                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = room.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MoppiTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${room.animeTitle} • Eps ${room.episodeNumber}",
                                fontSize = 11.sp,
                                color = MoppiTextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = if (room.isPrivate) "🔒 Private (PIN)" else "🌐 Public Room",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (room.isPrivate) BadgeSepuhAmber else MoppiGemTeal
                                )
                                Text(
                                    text = "👥 ${room.participantCount}/${room.maxParticipants}",
                                    fontSize = 10.sp,
                                    color = MoppiTextTertiary
                                )
                                if (room.isVoiceEnabled) {
                                    Text(
                                        text = "🎙️ Voice ON",
                                        fontSize = 10.sp,
                                        color = MoppiSapphire,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                if (room.isPrivate) {
                                    showPinDialogForRoom = room
                                    enteredPin = ""
                                    pinError = false
                                } else {
                                    onJoinRoom(room)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MoppiSurfaceMuted),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Gabung",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MoppiSapphire
                            )
                        }
                    }
                }
            }
        }
    }

    // PIN dialog for private rooms
    showPinDialogForRoom?.let { room ->
        AlertDialog(
            onDismissRequest = { showPinDialogForRoom = null },
            title = {
                Text(
                    text = "Room Private: Masukkan PIN",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MoppiTextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Room '${room.title}' dikunci oleh host. Masukkan PIN untuk bergabung (PIN demo: ${room.pinCode ?: "8899"}):",
                        fontSize = 12.sp,
                        color = MoppiTextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = {
                            enteredPin = it
                            pinError = false
                        },
                        placeholder = { Text("4 Digit PIN") },
                        isError = pinError,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (pinError) {
                        Text(
                            text = "PIN salah, silakan coba lagi.",
                            color = BadgeDeveloperRed,
                            fontSize = 11.sp
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
                    Text("Masuk")
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
