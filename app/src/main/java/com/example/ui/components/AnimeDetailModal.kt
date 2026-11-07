package com.example.ui.components

import androidx.compose.foundation.Image
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Anime
import com.example.data.model.Episode
import com.example.data.model.VideoPlayerMode
import com.example.ui.theme.*
import coil.compose.AsyncImage

@Composable
fun AnimeDetailModal(
    anime: Anime,
    onDismiss: () -> Unit,
    onPlayEpisode: (Anime, Episode, VideoPlayerMode) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("anime_detail_modal"),
        color = MoppiWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header with Back button and Poster/Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
            ) {
                if (!anime.posterUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = anime.posterUrl,
                        contentDescription = anime.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Image(
                        painter = painterResource(
                            id = anime.bannerDrawableRes ?: R.drawable.moppi_hero_banner
                        ),
                        contentDescription = anime.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.5f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.85f)
                                )
                            )
                        )
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .padding(14.dp)
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .align(Alignment.TopStart)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = Color.White
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "#${anime.rank} in Top Airing • ${anime.season}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MoppiCyan
                    )
                    Text(
                        text = anime.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = anime.japaneseTitle,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            // Body Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Play Action Buttons (Normal & VR)
                item {
                    val firstEp = anime.episodes.firstOrNull() ?: Episode(1, "Episode 1")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onPlayEpisode(anime, firstEp, VideoPlayerMode.NORMAL) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MoppiSapphire)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tonton Sekarang", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { onPlayEpisode(anime, firstEp, VideoPlayerMode.VR_CINEMA_360) },
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, MoppiCyan)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ViewInAr,
                                contentDescription = "VR Cinema",
                                tint = MoppiCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("16. Mode VR", color = MoppiCyan, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 29. SYNOPSIS ANIME
                item {
                    Column {
                        Text(
                            text = "29. Sinopsis Anime",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MoppiTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = anime.synopsis,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = MoppiTextSecondary
                        )
                    }
                }

                // Metadata Chips
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MoppiSurfaceSubtle,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MoppiBorder)
                        ) {
                            Text(
                                text = "Studio: ${anime.studio}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MoppiTextPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MoppiSurfaceSubtle,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MoppiBorder)
                        ) {
                            Text(
                                text = "⭐ Rating ${anime.rating}/10",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MoppiGold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MoppiSurfaceSubtle,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MoppiBorder)
                        ) {
                            Text(
                                text = "${anime.totalEpisodes} Episode",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MoppiSapphire,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // OtakuDesu Raw Stream & Fast Server Badge
                item {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MoppiCyan.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MoppiCyan.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Bolt,
                                contentDescription = "OtakuDesu Stream",
                                tint = MoppiCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "OtakuDesu Raw & Stream Server Aktif",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MoppiTextPrimary
                                )
                                Text(
                                    text = "Playback video instan resolusi tinggi tanpa buffering iklan",
                                    fontSize = 10.sp,
                                    color = MoppiTextSecondary
                                )
                            }
                        }
                    }
                }

                // OtakuDesu Batch Feature Card
                if (anime.batchLink != null) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MoppiViolet.copy(alpha = 0.08f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MoppiViolet.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(MoppiViolet.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Inventory2,
                                        contentDescription = "Batch Package",
                                        tint = MoppiViolet,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Paket Batch Lengkap (OtakuDesu)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MoppiViolet
                                    )
                                    Text(
                                        text = "Tersedia paket download dan streaming batch full series ${anime.totalEpisodes} episode dalam kualitas 360p - 1080p.",
                                        fontSize = 11.sp,
                                        color = MoppiTextSecondary,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Episode List
                item {
                    Text(
                        text = "Pilih Episode OtakuDesu",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MoppiTextPrimary
                    )
                }

                items(anime.episodes) { ep ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MoppiSurfaceSubtle,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MoppiBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPlayEpisode(anime, ep, VideoPlayerMode.NORMAL) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MoppiSapphire.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.PlayArrow,
                                        contentDescription = "Play",
                                        tint = MoppiSapphire,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = ep.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MoppiTextPrimary
                                    )
                                    Text(
                                        text = "Durasi ${ep.duration} • Auto Skip Intro",
                                        fontSize = 10.sp,
                                        color = MoppiTextTertiary
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Filled.ChevronRight,
                                contentDescription = "Open",
                                tint = MoppiTextTertiary
                            )
                        }
                    }
                }
            }
        }
    }
}
