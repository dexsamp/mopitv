package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AnimePet
import com.example.data.model.CoolFrame
import com.example.data.model.PetRarity
import com.example.ui.components.MoppiFrameAvatar
import com.example.ui.theme.*

@Composable
fun PetGachaScreen(
    pets: List<AnimePet>,
    frames: List<CoolFrame>,
    userGems: Int,
    isSpinning: Boolean,
    gachaResults: List<AnimePet>?,
    equippedPetId: String,
    equippedFrameId: String,
    onSpin: (isTenPull: Boolean) -> Unit,
    onDismissResult: () -> Unit,
    onEquipPet: (AnimePet) -> Unit,
    onEquipFrame: (CoolFrame) -> Unit,
    onAddGemsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: 100+ Anime Pets, 1: Cool Bingkai
    var selectedRarityFilter by remember { mutableStateOf<PetRarity?>(null) }

    val filteredPets = remember(pets, selectedRarityFilter) {
        if (selectedRarityFilter == null) pets else pets.filter { it.rarity == selectedRarityFilter }
    }

    val currentEquippedPet = remember(pets, equippedPetId) {
        pets.find { it.id == equippedPetId } ?: pets.firstOrNull()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MoppiSurfaceSubtle)
            .testTag("pet_gacha_screen")
    ) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 90.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Hero Gacha Spin Podium Card
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MoppiWhite,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MoppiBorder),
                    shadowElevation = 3.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Title & Gems counter
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "9. Moppi Gem Summon",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 17.sp,
                                    color = MoppiTextPrimary
                                )
                                Text(
                                    text = "Spin 100+ Anime Pet & Cool Bingkai",
                                    fontSize = 11.sp,
                                    color = MoppiTextTertiary
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MoppiSurfaceSubtle,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MoppiBorder),
                                modifier = Modifier.clickable { onAddGemsClick() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Diamond,
                                        contentDescription = "Gems",
                                        tint = MoppiGemTeal,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "$userGems Gems",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MoppiTextPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Center Mascot / Chibi Pet Display
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(MoppiSurfaceSubtle, MoppiSurfaceMuted)
                                    )
                                )
                                .border(
                                    3.dp,
                                    currentEquippedPet?.rarity?.color ?: MoppiSapphire,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.moppi_chibi_pet),
                                contentDescription = "Active Companion Pet",
                                modifier = Modifier.size(85.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Pet Aktif: ${currentEquippedPet?.name ?: "Moppi Pet"}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = currentEquippedPet?.rarity?.color ?: MoppiSapphire
                        )
                        Text(
                            text = "\"${currentEquippedPet?.quote ?: "Siap nonton anime bareng!"}\"",
                            fontSize = 11.sp,
                            color = MoppiTextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Gacha Spin Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { onSpin(false) },
                                enabled = !isSpinning && userGems >= 100,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("gacha_spin_single_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MoppiSapphire)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Refresh,
                                        contentDescription = "Spin",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "1x Spin (100 💎)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            Button(
                                onClick = { onSpin(true) },
                                enabled = !isSpinning && userGems >= 900,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("gacha_spin_ten_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MoppiViolet)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.AutoAwesome,
                                        contentDescription = "Spin 10x",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "10x Spin (900 💎)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Segmented Tab: 100+ Anime Pets vs 7. Cool Bingkai
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                            text = "24. 100+ Anime Pet (${pets.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
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
                            text = "7. Cool Bingkai (${frames.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            color = if (selectedTab == 1) Color.White else MoppiTextPrimary,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }
                }
            }

            // Tab 0: 100+ Anime Pet Roster
            if (selectedTab == 0) {
                // 25. Pet Berperingkat Rarity Filter
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "25. Filter Rarity Peringkat Pet",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MoppiTextPrimary
                            )
                            if (selectedRarityFilter != null) {
                                Text(
                                    text = "Reset",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MoppiSapphire,
                                    modifier = Modifier.clickable { selectedRarityFilter = null }
                                )
                            }
                        }

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (selectedRarityFilter == null) MoppiTextPrimary else MoppiWhite,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MoppiBorder),
                                    modifier = Modifier.clickable { selectedRarityFilter = null }
                                ) {
                                    Text(
                                        text = "Semua (${pets.size})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedRarityFilter == null) Color.White else MoppiTextPrimary,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                            items(PetRarity.values()) { rarity ->
                                val isSel = selectedRarityFilter == rarity
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isSel) rarity.color else MoppiWhite,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, rarity.color),
                                    modifier = Modifier.clickable { selectedRarityFilter = rarity }
                                ) {
                                    Text(
                                        text = "${rarity.label} (${pets.count { it.rarity == rarity }})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) Color.White else rarity.color,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                items(filteredPets) { pet ->
                    val isEquipped = pet.id == equippedPetId
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MoppiWhite,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isEquipped) pet.rarity.color else MoppiBorder
                        ),
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(pet.rarity.color.copy(alpha = 0.15f))
                                    .border(1.5.dp, pet.rarity.color, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = pet.iconEmoji,
                                    fontSize = 22.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = pet.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MoppiTextPrimary
                                    )
                                }
                                Text(
                                    text = "${pet.animeOrigin} • ${pet.powerBonus}",
                                    fontSize = 11.sp,
                                    color = MoppiTextSecondary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = pet.rarity.color.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = pet.rarity.label.uppercase(),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = pet.rarity.color,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            if (pet.isOwned) {
                                Button(
                                    onClick = { onEquipPet(pet) },
                                    enabled = !isEquipped,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isEquipped) MoppiGemTeal else MoppiSurfaceMuted,
                                        disabledContainerColor = MoppiGemTeal
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (isEquipped) "Dipakai ✓" else "Pakai",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isEquipped) Color.White else MoppiTextPrimary
                                    )
                                }
                            } else {
                                Text(
                                    text = "Belum punya",
                                    fontSize = 10.sp,
                                    color = MoppiTextTertiary
                                )
                            }
                        }
                    }
                }
            } else {
                // Tab 1: 7. Cool Bingkai
                items(frames) { frame ->
                    val isEquipped = frame.id == equippedFrameId
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MoppiWhite,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isEquipped) MoppiSapphire else MoppiBorder
                        ),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MoppiFrameAvatar(
                                size = 48.dp,
                                frame = frame,
                                petEmoji = "✨"
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = frame.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MoppiTextPrimary
                                )
                                Text(
                                    text = frame.description,
                                    fontSize = 11.sp,
                                    color = MoppiTextSecondary,
                                    lineHeight = 15.sp
                                )
                            }

                            if (frame.isOwned) {
                                Button(
                                    onClick = { onEquipFrame(frame) },
                                    enabled = !isEquipped,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isEquipped) MoppiSapphire else MoppiSurfaceMuted,
                                        disabledContainerColor = MoppiSapphire
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (isEquipped) "Dipakai ✓" else "Pasang",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isEquipped) Color.White else MoppiTextPrimary
                                    )
                                }
                            } else {
                                Button(
                                    onClick = { onEquipFrame(frame) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MoppiGemTeal),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${frame.gemCost} 💎",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Gacha Result Modal Overlay
        gachaResults?.let { results ->
            AlertDialog(
                onDismissRequest = onDismissResult,
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "🎉 HASIL SPIN SUMMON!",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = MoppiTextPrimary
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 350.dp)
                    ) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(results) { pet ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = pet.rarity.color.copy(alpha = 0.1f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, pet.rarity.color)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = pet.iconEmoji, fontSize = 24.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = pet.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = MoppiTextPrimary
                                            )
                                            Text(
                                                text = "${pet.rarity.label.uppercase()} • ${pet.animeOrigin}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                color = pet.rarity.color
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = onDismissResult,
                        colors = ButtonDefaults.buttonColors(containerColor = MoppiSapphire),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Klaim Semua Hadiah")
                    }
                }
            )
        }
    }
}
