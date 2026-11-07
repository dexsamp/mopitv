package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SupportedLanguage
import com.example.ui.theme.*

@Composable
fun AdminPanelDialog(
    onDismiss: () -> Unit,
    onBroadcast: (title: String, message: String, isUrgent: Boolean) -> Unit,
    onIssueWarn: (targetUser: String, reason: String) -> Unit,
    onAddFreeGems: (Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isUrgent by remember { mutableStateOf(false) }

    var warnUser by remember { mutableStateOf("") }
    var warnReason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.AdminPanelSettings,
                    contentDescription = "Admin Panel",
                    tint = BadgeDeveloperRed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "19. System Panel Admin",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MoppiTextPrimary
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "20. System Announcement Admin",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MoppiTextPrimary
                    )
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("Judul Pengumuman") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        placeholder = { Text("Isi Pesan Broadcast...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Tandai Mendesak (Urgent)", fontSize = 11.sp)
                        Switch(checked = isUrgent, onCheckedChange = { isUrgent = it })
                    }
                    Button(
                        onClick = {
                            if (title.isNotBlank() && message.isNotBlank()) {
                                onBroadcast(title, message, isUrgent)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BadgeDeveloperRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Broadcast ke Seluruh User")
                    }
                }

                item {
                    HorizontalDivider(color = MoppiBorder, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "18. System Warn / Peringatan Pengguna",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MoppiTextPrimary
                    )
                    OutlinedTextField(
                        value = warnUser,
                        onValueChange = { warnUser = it },
                        placeholder = { Text("Username target (cth: MoppiMaster)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = warnReason,
                        onValueChange = { warnReason = it },
                        placeholder = { Text("Alasan pelanggaran...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            if (warnUser.isNotBlank() && warnReason.isNotBlank()) {
                                onIssueWarn(warnUser, warnReason)
                                warnUser = ""
                                warnReason = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BadgeSepuhAmber),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Kirim Official Warning (Strike)")
                    }
                }

                item {
                    HorizontalDivider(color = MoppiBorder, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Developer Tool: Tambah Moppi Gem",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MoppiTextPrimary
                    )
                    Button(
                        onClick = { onAddFreeGems(500) },
                        colors = ButtonDefaults.buttonColors(containerColor = MoppiGemTeal),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("+500 Moppi Gem Instan")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}

@Composable
fun GiftDialog(
    userGems: Int,
    onDismiss: () -> Unit,
    onSendGift: (targetUser: String, amount: Int, giftPrem: Boolean) -> Unit
) {
    var targetUser by remember { mutableStateOf("") }
    var gemAmount by remember { mutableStateOf("100") }
    var giftPrem by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.CardGiftcard,
                    contentDescription = "Gift",
                    tint = MoppiGemTeal,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "12. Give Gem / Premium",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MoppiTextPrimary
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Kirim Moppi Gem atau hadiahkan VIP Premium kepada teman wibu kamu!",
                    fontSize = 11.sp,
                    color = MoppiTextSecondary
                )
                OutlinedTextField(
                    value = targetUser,
                    onValueChange = { targetUser = it },
                    placeholder = { Text("Username penerima (cth: BudiAnime)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = gemAmount,
                    onValueChange = { gemAmount = it },
                    placeholder = { Text("Jumlah Moppi Gem (cth: 100)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Sertakan Tiket VIP Premium (1 Bulan)", fontSize = 11.sp)
                    Switch(checked = giftPrem, onCheckedChange = { giftPrem = it })
                }
                Text(
                    text = "Saldo Moppi Gem kamu saat ini: $userGems 💎",
                    fontSize = 11.sp,
                    color = MoppiTextTertiary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (targetUser.isNotBlank()) {
                        val amount = gemAmount.toIntOrNull() ?: 0
                        onSendGift(targetUser, amount, giftPrem)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MoppiGemTeal)
            ) {
                Text("Kirim Hadiah")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun LanguageSelectorDialog(
    languages: List<SupportedLanguage>,
    currentLang: SupportedLanguage,
    onSelectLanguage: (SupportedLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    var searchLang by remember { mutableStateOf("") }
    val filtered = remember(languages, searchLang) {
        if (searchLang.isBlank()) languages else languages.filter {
            it.name.contains(searchLang, ignoreCase = true) ||
                    it.nativeName.contains(searchLang, ignoreCase = true) ||
                    it.code.contains(searchLang, ignoreCase = true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Language,
                    contentDescription = "Language",
                    tint = MoppiSapphire,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "6. Pilihan 50 Bahasa",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MoppiTextPrimary
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = searchLang,
                    onValueChange = { searchLang = it },
                    placeholder = { Text("Cari bahasa...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filtered) { lang ->
                        val isSelected = lang.code == currentLang.code
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MoppiSapphire else MoppiWhite,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MoppiBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectLanguage(lang) }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = lang.flag, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = lang.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (isSelected) Color.White else MoppiTextPrimary
                                        )
                                        Text(
                                            text = lang.nativeName,
                                            fontSize = 10.sp,
                                            color = if (isSelected) Color.White.copy(alpha = 0.8f) else MoppiTextTertiary
                                        )
                                    }
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "Selected",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}

@Composable
fun CreateRoomDialog(
    onDismiss: () -> Unit,
    onCreate: (title: String, anime: String, isPrivate: Boolean, pin: String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var animeTitle by remember { mutableStateOf("Solo Leveling: Arise") }
    var isPrivate by remember { mutableStateOf(false) }
    var pinCode by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Buat Room Nobar Baru",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MoppiTextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("Nama Room (cth: Nobar Chill Eps 12)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = animeTitle,
                    onValueChange = { animeTitle = it },
                    placeholder = { Text("Judul Anime") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Kunci dengan PIN (Private)", fontSize = 12.sp)
                    Switch(checked = isPrivate, onCheckedChange = { isPrivate = it })
                }
                if (isPrivate) {
                    OutlinedTextField(
                        value = pinCode,
                        onValueChange = { pinCode = it },
                        placeholder = { Text("4 Digit PIN (cth: 1234)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onCreate(title, animeTitle, isPrivate, if (isPrivate) pinCode else null)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MoppiSapphire)
            ) {
                Text("Buat Sekarang")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
