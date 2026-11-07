package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AnimePet
import com.example.data.model.PetRarity
import com.example.ui.theme.*

/**
 * GachaPet screen with a 3D-like animation effect where users can pull for collectible anime pet companions.
 */
@Composable
fun GachaPet(
    pets: List<AnimePet>,
    userGems: Int,
    isSpinning: Boolean,
    gachaResults: List<AnimePet>?,
    equippedPetId: String,
    onSpin: (isTenPull: Boolean) -> Unit,
    onDismissResult: () -> Unit,
    onEquipPet: (AnimePet) -> Unit,
    onAddGemsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedRarityFilter by remember { mutableStateOf<PetRarity?>(null) }
    val density = LocalDensity.current

    val currentEquippedPet = remember(pets, equippedPetId) {
        pets.find { it.id == equippedPetId } ?: pets.firstOrNull()
    }

    val filteredPets = remember(pets, selectedRarityFilter) {
        if (selectedRarityFilter == null) pets else pets.filter { it.rarity == selectedRarityFilter }
    }

    // Interactive 3D Card Tilt State for Showcase Card
    var cardTiltOffset by remember { mutableStateOf(Offset.Zero) }
    val animatedTiltX by animateFloatAsState(
        targetValue = cardTiltOffset.y.coerceIn(-20f, 20f),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "tiltX"
    )
    val animatedTiltY by animateFloatAsState(
        targetValue = -cardTiltOffset.x.coerceIn(-20f, 20f),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "tiltY"
    )

    // 3D Continuous Floating / Orbit Animation
    val infiniteTransition = rememberInfiniteTransition(label = "3d_float")
    val floatRotationY by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotationY"
    )
    val floatGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MoppiSurfaceSubtle)
            .testTag("gacha_pet_screen")
    ) {
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Header: Balance & Title
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Gacha Pet Summon",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = MoppiTextPrimary
                        )
                        Text(
                            text = "Tarik kartu pet anime 3D & kumpulkan 100+ companion",
                            fontSize = 11.sp,
                            color = MoppiTextTertiary
                        )
                    }

                    // Gem Counter Chip
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MoppiWhite,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MoppiBorder),
                        modifier = Modifier.clickable { onAddGemsClick() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Diamond,
                                contentDescription = "Moppi Gem",
                                tint = MoppiGemTeal,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$userGems Gems",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MoppiTextPrimary
                            )
                        }
                    }
                }
            }

            // 3D HOLOGRAPHIC SUMMON PODIUM CARD
            item {
                val pet = currentEquippedPet
                val rarityColor = pet?.rarity?.color ?: MoppiSapphire

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    cardTiltOffset += dragAmount * 0.4f
                                },
                                onDragEnd = { cardTiltOffset = Offset.Zero },
                                onDragCancel = { cardTiltOffset = Offset.Zero }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // 3D Card Layer
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MoppiWhite,
                        shadowElevation = 8.dp,
                        border = androidx.compose.foundation.BorderStroke(2.dp, rarityColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                rotationX = animatedTiltX
                                rotationY = animatedTiltY + floatRotationY
                                cameraDistance = 16f * density.density
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            rarityColor.copy(alpha = 0.12f * floatGlowAlpha),
                                            MoppiWhite
                                        )
                                    )
                                )
                                .padding(20.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Rarity Badge & 3D Tag
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = rarityColor
                                    ) {
                                        Text(
                                            text = "${pet?.rarity?.label?.uppercase()} TIER",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 10.sp,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }

                                    Text(
                                        text = "3D INTERACTIVE TILT (Sentuh & Geser)",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MoppiTextTertiary
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Holographic Companion Avatar Container
                                Box(
                                    modifier = Modifier
                                        .size(130.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.sweepGradient(
                                                listOf(
                                                    rarityColor.copy(alpha = 0.6f),
                                                    MoppiCyan.copy(alpha = 0.6f),
                                                    rarityColor.copy(alpha = 0.6f)
                                                )
                                            )
                                        )
                                        .padding(4.dp)
                                        .clip(CircleShape)
                                        .background(MoppiWhite),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.moppi_chibi_pet),
                                        contentDescription = pet?.name ?: "Pet",
                                        modifier = Modifier.size(105.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = pet?.name ?: "Moppi Pet",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = MoppiTextPrimary
                                )
                                Text(
                                    text = "Origin: ${pet?.animeOrigin ?: "Anime Universe"} • Lv.${pet?.level ?: 1}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MoppiSapphire
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "\"${pet?.quote ?: "Aku siap bertualang bersamamu!"}\"",
                                    fontSize = 12.sp,
                                    color = MoppiTextSecondary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Efek Pasif: ${pet?.powerBonus ?: "+30% EXP Stream"}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MoppiGemTeal
                                )

                                Spacer(modifier = Modifier.height(18.dp))

                                // 3D Summon Action Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = { onSpin(false) },
                                        enabled = !isSpinning && userGems >= 100,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("gachapet_pull_1x"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MoppiSapphire)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Filled.Refresh,
                                                contentDescription = "Pull 1x",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isSpinning) "Pulling..." else "Pull 1x (100 💎)",
                                                fontSize = 11.sp,
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
                                            .testTag("gachapet_pull_10x"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MoppiViolet)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Filled.AutoAwesome,
                                                contentDescription = "Pull 10x",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isSpinning) "Summoning..." else "Pull 10x (900 💎)",
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
            }

            // Rarity Filter Horizontal Chips
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Koleksi Anime Pet Companion (${filteredPets.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MoppiTextPrimary
                        )
                        if (selectedRarityFilter != null) {
                            Text(
                                text = "Semua Rarity",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MoppiSapphire,
                                modifier = Modifier.clickable { selectedRarityFilter = null }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (selectedRarityFilter == null) MoppiTextPrimary else MoppiWhite,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MoppiBorder),
                                modifier = Modifier.clickable { selectedRarityFilter = null }
                            ) {
                                Text(
                                    text = "All (${pets.size})",
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

            // Collectible Pet Cards List
            items(filteredPets) { petItem ->
                val isEquipped = petItem.id == equippedPetId
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MoppiWhite,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isEquipped) petItem.rarity.color else MoppiBorder
                    ),
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(petItem.rarity.color.copy(alpha = 0.15f))
                                .border(1.5.dp, petItem.rarity.color, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = petItem.iconEmoji,
                                fontSize = 22.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = petItem.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MoppiTextPrimary
                                )
                            }
                            Text(
                                text = "${petItem.animeOrigin} • ${petItem.powerBonus}",
                                fontSize = 11.sp,
                                color = MoppiTextSecondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = petItem.rarity.color.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = petItem.rarity.label.uppercase(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = petItem.rarity.color,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        if (petItem.isOwned) {
                            Button(
                                onClick = { onEquipPet(petItem) },
                                enabled = !isEquipped,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isEquipped) MoppiGemTeal else MoppiSurfaceMuted,
                                    disabledContainerColor = MoppiGemTeal
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (isEquipped) "Aktif ✓" else "Pakai",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isEquipped) Color.White else MoppiTextPrimary
                                )
                            }
                        } else {
                            Text(
                                text = "Gacha to Unlock",
                                fontSize = 10.sp,
                                color = MoppiTextTertiary
                            )
                        }
                    }
                }
            }
        }

        // 3D Pull Reveal Modal Overlay
        gachaResults?.let { results ->
            AlertDialog(
                onDismissRequest = onDismissResult,
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "✨ HASIL PULL ANIME PET 3D",
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
                            .heightIn(max = 380.dp)
                    ) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(results) { pet ->
                                // 3D Card Animation for Pulled Pets
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = pet.rarity.color.copy(alpha = 0.12f),
                                    border = androidx.compose.foundation.BorderStroke(1.5.dp, pet.rarity.color),
                                    shadowElevation = 3.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .graphicsLayer {
                                            rotationY = 360f
                                            cameraDistance = 12f * density.density
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = pet.iconEmoji, fontSize = 28.sp)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = pet.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = MoppiTextPrimary
                                            )
                                            Text(
                                                text = "${pet.rarity.label.uppercase()} • ${pet.animeOrigin}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = pet.rarity.color
                                            )
                                            Text(
                                                text = pet.quote,
                                                fontSize = 10.sp,
                                                color = MoppiTextSecondary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
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
                        Text("Koleksi Semua Pet")
                    }
                }
            )
        }
    }
}
