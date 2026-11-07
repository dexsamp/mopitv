package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AnimePet
import com.example.data.model.PetRarity
import com.example.ui.theme.*

@Composable
fun MoppiFloatingPet(
    equippedPet: AnimePet?,
    speechQuote: String?,
    onPetClicked: () -> Unit,
    onDismissQuote: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(true) }

    // Floating breathing animation
    val infiniteTransition = rememberInfiniteTransition(label = "pet_float")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetY"
    )

    Box(
        modifier = modifier
            .padding(16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Bottom
        ) {
            // Speech Bubble
            AnimatedVisibility(
                visible = speechQuote != null && isExpanded,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { it / 2 }
            ) {
                Surface(
                    shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 14.dp, bottomEnd = 2.dp),
                    color = MoppiWhite,
                    shadowElevation = 6.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MoppiBorder),
                    modifier = Modifier
                        .padding(bottom = 6.dp)
                        .widthIn(max = 220.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = equippedPet?.name ?: "Moppi Pet",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = equippedPet?.rarity?.color ?: MoppiSapphire
                            )
                            IconButton(
                                onClick = onDismissQuote,
                                modifier = Modifier.size(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Tutup",
                                    tint = MoppiTextTertiary,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = speechQuote ?: "",
                            fontSize = 12.sp,
                            color = MoppiTextPrimary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Pet Companion Avatar / Mascot
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    shape = CircleShape,
                    color = MoppiWhite,
                    shadowElevation = 8.dp,
                    border = androidx.compose.foundation.BorderStroke(
                        2.dp,
                        equippedPet?.rarity?.color ?: MoppiSapphire
                    ),
                    modifier = Modifier
                        .offset(y = offsetY.dp)
                        .size(56.dp)
                        .clickable { onPetClicked() }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    listOf(MoppiWhite, MoppiSurfaceSubtle)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.moppi_chibi_pet),
                            contentDescription = "Anime Companion Pet",
                            modifier = Modifier.size(46.dp)
                        )

                        // Rarity Tier Mini Pill
                        if (equippedPet != null) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .offset(y = 2.dp)
                                    .background(
                                        color = equippedPet.rarity.color,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = equippedPet.rarity.label.take(3).uppercase(),
                                    color = MoppiWhite,
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
