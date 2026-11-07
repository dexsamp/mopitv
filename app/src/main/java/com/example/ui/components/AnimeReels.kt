package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.AnimeShort
import com.example.ui.theme.*

/**
 * Vertical scrolling 'Anime Reels' component using a LazyColumn
 * that supports full-screen video previews of trending anime clips.
 */
@Composable
fun AnimeReels(
    shorts: List<AnimeShort>,
    onToggleLike: (String) -> Unit,
    onWatchFullAnime: (String) -> Unit,
    onRefreshClips: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("anime_reels_component")
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = snapFlingBehavior,
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(shorts, key = { _, short -> short.id }) { index, short ->
                var isItemPlaying by remember { mutableStateOf(true) }
                var showCommentsSheet by remember { mutableStateOf(false) }

                val isCurrentReel = remember(listState.firstVisibleItemIndex) {
                    listState.firstVisibleItemIndex == index
                }

                Box(
                    modifier = Modifier
                        .fillParentMaxHeight()
                        .fillMaxWidth()
                        .clickable { isItemPlaying = !isItemPlaying }
                ) {
                    // Activate real video playback only on the currently visible reel
                    if (isCurrentReel) {
                        RealAnimeVideoPlayer(
                            videoUrl = short.videoStreamUrl,
                            thumbnailUrl = short.thumbnailUrl,
                            isPlaying = isItemPlaying,
                            isLooping = true,
                            isMuted = false,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // High resolution poster backdrop for offscreen reels
                        if (!short.thumbnailUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = short.thumbnailUrl,
                                contentDescription = short.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.moppi_hero_banner),
                                contentDescription = short.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    // Cinematic Vignette Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Black.copy(alpha = 0.45f),
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.88f)
                                    )
                                )
                            )
                    )

                    // Play / Pause Indicator
                    if (!isItemPlaying) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier.size(46.dp)
                            )
                        }
                    }

                    // Top Bar: Anime Clip & Sync indicator
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.Black.copy(alpha = 0.6f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(MoppiCyan)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Live Anime Stream Preview HD",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        // Refresh stream action
                        IconButton(
                            onClick = onRefreshClips,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Refresh Trending Clips",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Floating Right Actions Bar (Likes, Comments, Share, Audio)
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = 90.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Like Button with heart burst
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = { onToggleLike(short.id) },
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.45f))
                            ) {
                                Icon(
                                    imageVector = if (short.isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                    contentDescription = "Like",
                                    tint = if (short.isLiked) BadgeDeveloperRed else Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${short.likesCount / 1000}k",
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Comment Button
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = { showCommentsSheet = true },
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.45f))
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ModeComment,
                                    contentDescription = "Comment",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${short.commentsCount}",
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Share Button
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = { /* Share clip */ },
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.45f))
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Share,
                                    contentDescription = "Share",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${short.sharesCount}",
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Bottom Left Video Metadata & "Tonton Anime Lengkap" CTA
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 16.dp, end = 90.dp, bottom = 90.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            MoppiFrameAvatar(
                                size = 34.dp,
                                role = short.uploaderRole,
                                petEmoji = "🔥"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "@${short.uploaderName}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            if (short.uploaderRole == com.example.data.model.UserRole.DEVELOPER) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Verified Dev",
                                    tint = BadgeDeveloperRed,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = short.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            lineHeight = 17.sp,
                            maxLines = 2
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = short.tags.joinToString(" "),
                            fontSize = 11.sp,
                            color = MoppiCyan,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Full Episode Streaming CTA
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MoppiSapphire,
                            modifier = Modifier
                                .clickable { onWatchFullAnime(short.animeSource) }
                                .testTag("watch_full_anime_cta_${short.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayCircleFilled,
                                    contentDescription = "Watch Full",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Tonton Anime Lengkap (${short.animeSource})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Comment Bottom Sheet Modal
                if (showCommentsSheet) {
                    AlertDialog(
                        onDismissRequest = { showCommentsSheet = false },
                        title = {
                            Text(
                                text = "Komentar Anime Reels (${short.commentsCount})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MoppiTextPrimary
                            )
                        },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "🔥 WibuSepuh_99: Gila animasinya MAPPA/Ufotable makin gokil!",
                                    fontSize = 12.sp,
                                    color = MoppiTextPrimary
                                )
                                Text(
                                    text = "✨ AnyaFan: Cutscene ini waktu lawan boss beneran merinding!",
                                    fontSize = 12.sp,
                                    color = MoppiTextSecondary
                                )
                                Text(
                                    text = "💎 MoppiMaster: Jangan lupa tonton full episodenya langsung di MoppiTV ya guys!",
                                    fontSize = 12.sp,
                                    color = MoppiSapphire
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showCommentsSheet = false }) {
                                Text("Tutup")
                            }
                        }
                    )
                }
            }
        }
    }
}
