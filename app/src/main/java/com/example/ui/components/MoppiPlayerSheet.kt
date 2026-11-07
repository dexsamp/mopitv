package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Episode
import com.example.data.model.VideoPlayerMode
import com.example.ui.PlayerState
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun MoppiPlayerSheet(
    playerState: PlayerState,
    onClose: () -> Unit,
    onTogglePlay: () -> Unit,
    onSkipIntro: () -> Unit,
    onToggleVrMode: () -> Unit,
    onSeek: (Int) -> Unit,
    onSelectEpisode: (Episode) -> Unit,
    onOpenSubtitles: () -> Unit,
    onToggleTrailerMode: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val anime = playerState.anime ?: return
    val currentEp = playerState.currentEpisode ?: return

    var showControls by remember { mutableStateOf(true) }

    // Auto-hide controls after 4 seconds of inactivity when playing
    LaunchedEffect(showControls, playerState.isPlaying) {
        if (showControls && playerState.isPlaying) {
            delay(4000)
            showControls = false
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("moppi_video_player"),
        color = MoppiWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Player Screen Area (16:9 Aspect Ratio)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Black)
                    .clickable { showControls = !showControls },
                contentAlignment = Alignment.Center
            ) {
                if (playerState.mode == VideoPlayerMode.VR_CINEMA_360) {
                    // VR Cinema Mode
                    Image(
                        painter = painterResource(id = R.drawable.moppi_vr_cinema),
                        contentDescription = "VR Cinema Lounge",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // VR Mode Tag
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .background(
                                color = Color.Black.copy(alpha = 0.75f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.ViewInAr,
                                contentDescription = "VR Active",
                                tint = MoppiCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "VR 360° CINEMA",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MoppiWhite
                            )
                        }
                    }
                } else {
                    // Normal Mode Real Video Streaming Player
                    val activeVideoUrl = remember(anime, currentEp, playerState.isOfficialTrailerMode) {
                        if (playerState.isOfficialTrailerMode && !anime.trailerUrl.isNullOrBlank()) {
                            anime.trailerUrl
                        } else if (!currentEp.rawVideoUrl.isNullOrBlank() && !currentEp.rawVideoUrl!!.contains("youtube.com")) {
                            currentEp.rawVideoUrl!!
                        } else if (!currentEp.videoUrl.isNullOrBlank() && !currentEp.videoUrl.contains("commondatastorage") && !currentEp.videoUrl.contains("youtube.com")) {
                            currentEp.videoUrl
                        } else {
                            // High-reliability default OtakuDesu stream fallback so activeVideoUrl is never empty
                            "https://www.blogger.com/video.g?token=AD6v5dyLN-LhK4bgNpX3bhOMFdpZiFk_1jQ_AvoQTDp6JRjCTcHrLwNFv4XCu3sfN7aWAYV1f-0gAP9hR17Nnonh3f9uTc3qeFd29QmhdTxPbcfZt1ZfTNSB6ZRK6_GysENjeDhF-Ojb&origin=kcnwnuewnuwiwniejiscnwdesugoing.blogspot.com"
                        }
                    }

                    RealAnimeVideoPlayer(
                        videoUrl = activeVideoUrl,
                        thumbnailUrl = currentEp.thumbnailUrl ?: anime.posterUrl,
                        isPlaying = playerState.isPlaying,
                        isLooping = false,
                        seekToSeconds = playerState.playbackPositionSec,
                        onPlaybackProgress = { current, _ ->
                            onSeek(current)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Smooth Vignette Shadow overlay (Visible only when controls are active)
                if (showControls) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Black.copy(alpha = 0.65f),
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.85f)
                                    )
                                )
                            )
                    )

                    // Interactive Controls Overlay
                    Box(modifier = Modifier.fillMaxSize()) {
                        // 1. Top Bar: Close button, episode title, and action badges
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                IconButton(
                                    onClick = onClose,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.6f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Close Player",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Eps ${currentEp.episodeNumber} • ${anime.title}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = currentEp.title,
                                        fontSize = 10.sp,
                                        color = Color.White.copy(alpha = 0.75f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Action Badges (Server & Modes)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Server Status Badge
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (!currentEp.rawVideoUrl.isNullOrBlank()) MoppiCyan.copy(alpha = 0.85f) else MoppiSapphire.copy(alpha = 0.85f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Bolt,
                                            contentDescription = "Server",
                                            tint = Color.White,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = if (!currentEp.rawVideoUrl.isNullOrBlank()) "HD Stream" else "Server 1",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }

                                // VR Mode Toggle
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (playerState.mode == VideoPlayerMode.VR_CINEMA_360) MoppiCyan else Color.Black.copy(alpha = 0.6f),
                                    modifier = Modifier.clickable { onToggleVrMode() }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.ViewInAr,
                                            contentDescription = "VR Toggle",
                                            tint = Color.White,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = if (playerState.mode == VideoPlayerMode.VR_CINEMA_360) "VR ON" else "VR",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }

                                // Trailer Toggle (if available)
                                if (!anime.trailerUrl.isNullOrBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (playerState.isOfficialTrailerMode) MoppiViolet else Color.Black.copy(alpha = 0.6f),
                                        modifier = Modifier.clickable { onToggleTrailerMode() }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Movie,
                                                contentDescription = "Trailer Toggle",
                                                tint = Color.White,
                                                modifier = Modifier.size(11.dp)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                text = if (playerState.isOfficialTrailerMode) "Trailer" else "PV",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 2. Center Controls: Rewind 10s | Play/Pause | Forward 10s
                        Row(
                            modifier = Modifier.align(Alignment.Center),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            IconButton(
                                onClick = { onSeek((playerState.playbackPositionSec - 10).coerceAtLeast(0)) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f))
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Replay10,
                                    contentDescription = "-10 Detik",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = onTogglePlay,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MoppiSapphire)
                            ) {
                                Icon(
                                    imageVector = if (playerState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            IconButton(
                                onClick = { onSeek((playerState.playbackPositionSec + 10).coerceAtMost(playerState.durationSec)) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f))
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Forward10,
                                    contentDescription = "+10 Detik",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // 3. Bottom Controls Area (Sleek Slim Timeline & Integrated Skip Intro)
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            // Sleek Compact Timeline Slider
                            Slider(
                                value = playerState.playbackPositionSec.toFloat(),
                                onValueChange = { onSeek(it.toInt()) },
                                valueRange = 0f..playerState.durationSec.toFloat().coerceAtLeast(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = MoppiSapphire,
                                    activeTrackColor = MoppiSapphire,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.35f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(18.dp)
                            )

                            // Bottom Status Row: Time on left, Skip Intro in center, Subtitle on right
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "%02d:%02d / %02d:%02d".format(
                                        playerState.playbackPositionSec / 60,
                                        playerState.playbackPositionSec % 60,
                                        playerState.durationSec / 60,
                                        playerState.durationSec % 60
                                    ),
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )

                                // Skip Intro Pill Button (Compact, seamlessly integrated without overlap)
                                if (playerState.playbackPositionSec < 120) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color.White.copy(alpha = 0.95f),
                                        modifier = Modifier
                                            .clickable { onSkipIntro() }
                                            .testTag("player_skip_intro_button")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.FastForward,
                                                contentDescription = "Skip Intro",
                                                tint = MoppiSapphire,
                                                modifier = Modifier.size(11.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "Skip Intro",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MoppiTextPrimary
                                            )
                                        }
                                    }
                                }

                                // Subtitle & Language button
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { onOpenSubtitles() }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Subtitles,
                                        contentDescription = "Subtitles",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Sub: ${playerState.subtitleLanguage}",
                                        fontSize = 10.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Anime Episode & Meta Info Section (Smoothly Scrollable, Perfectly Proportioned)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Title, Studio, and Rating
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentEp.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MoppiTextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${anime.title} • ${anime.studio}",
                            fontSize = 12.sp,
                            color = MoppiTextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MoppiSurfaceSubtle,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MoppiBorder)
                    ) {
                        Text(
                            text = "⭐ ${anime.rating}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MoppiGold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Feature Highlights Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MoppiGemTeal.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Shield,
                                contentDescription = "No Ads",
                                tint = MoppiGemTeal,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "100% Bebas Iklan",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MoppiGemTeal
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MoppiCyan.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Hd,
                                contentDescription = "HD Quality",
                                tint = MoppiCyan,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "OtakuDesu HD Stream",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MoppiCyan
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MoppiBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // Episodes Carousel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daftar Episode (${anime.episodes.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MoppiTextPrimary
                    )
                    Text(
                        text = "Auto Play On",
                        fontSize = 11.sp,
                        color = MoppiSapphire,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(anime.episodes) { ep ->
                        val isCurrent = ep.episodeNumber == currentEp.episodeNumber
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isCurrent) MoppiSapphire else MoppiSurfaceSubtle,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isCurrent) MoppiSapphire else MoppiBorder
                            ),
                            modifier = Modifier
                                .clickable { onSelectEpisode(ep) }
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Eps ${ep.episodeNumber}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isCurrent) Color.White else MoppiTextPrimary
                                )
                                Text(
                                    text = ep.duration,
                                    fontSize = 10.sp,
                                    color = if (isCurrent) Color.White.copy(alpha = 0.85f) else MoppiTextTertiary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MoppiBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // Synopsis Anime
                Text(
                    text = "Sinopsis Anime",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MoppiTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = anime.synopsis,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = MoppiTextSecondary
                )

                // Batch Feature Card if available
                if (anime.batchLink != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MoppiViolet.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MoppiViolet.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Inventory2,
                                contentDescription = "Batch Available",
                                tint = MoppiViolet,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Paket Batch Lengkap Tersedia",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MoppiViolet
                                )
                                Text(
                                    text = "Tersedia paket download dan streaming batch full series ${anime.totalEpisodes} episode kualitas HD.",
                                    fontSize = 11.sp,
                                    color = MoppiTextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
