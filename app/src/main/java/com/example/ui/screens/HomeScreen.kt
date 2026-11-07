package com.example.ui.screens

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
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.AdminAnnouncement
import com.example.data.model.Anime
import com.example.data.model.VideoPlayerMode
import com.example.ui.theme.*

import com.example.data.api.StenlyScheduleDay
import com.example.data.api.StenlyGenreItem

@Composable
fun HomeScreen(
    animeList: List<Anime>,
    announcements: List<AdminAnnouncement>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedGenre: String,
    onGenreSelect: (String) -> Unit,
    scheduleDays: List<StenlyScheduleDay> = emptyList(),
    selectedScheduleDay: String = "Semua",
    onSelectScheduleDay: (String) -> Unit = {},
    genresList: List<StenlyGenreItem> = emptyList(),
    onAnimeClick: (Anime) -> Unit,
    onDirectPlay: (Anime, VideoPlayerMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val daysList = remember {
        listOf("Semua", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")
    }

    val displayGenres = remember(genresList) {
        if (genresList.isNotEmpty()) {
            listOf("All") + genresList.mapNotNull { it.name }.distinct().take(15)
        } else {
            listOf("All", "Action", "Fantasy", "Adventure", "Supernatural", "Comedy", "Isekai", "Romance", "Mystery", "Sci-Fi")
        }
    }

    // Filter anime
    val filteredAnime = remember(animeList, searchQuery, selectedGenre, selectedScheduleDay, scheduleDays) {
        animeList.filter { anime ->
            val matchQuery = searchQuery.isBlank() || anime.title.contains(searchQuery, ignoreCase = true) ||
                    anime.synopsis.contains(searchQuery, ignoreCase = true)
            val matchGenre = selectedGenre == "All" || anime.genres.any { it.contains(selectedGenre, ignoreCase = true) }
            val matchSchedule = if (selectedScheduleDay == "Semua") {
                true
            } else {
                val dayObj = scheduleDays.find { it.day.equals(selectedScheduleDay, ignoreCase = true) }
                val isInDayObj = dayObj?.anime?.any { it.title?.contains(anime.title, ignoreCase = true) == true || anime.title.contains(it.title ?: "", ignoreCase = true) } == true
                val matchesDayUpdated = anime.dayUpdated?.contains(selectedScheduleDay, ignoreCase = true) == true
                isInDayObj || matchesDayUpdated
            }
            matchQuery && matchGenre && matchSchedule
        }
    }

    val topRanked = remember(animeList) {
        animeList.sortedBy { it.rank }.take(5)
    }

    val heroAnime = animeList.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MoppiSurfaceSubtle)
            .testTag("home_screen_feed"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // 20. SYSTEM ANNOUNCEMENT ADMIN BANNER
        if (announcements.isNotEmpty()) {
            item {
                val latest = announcements.first()
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MoppiWhite,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MoppiViolet.copy(alpha = 0.3f)),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MoppiViolet.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Campaign,
                                contentDescription = "Announcement",
                                tint = MoppiViolet,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = latest.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MoppiTextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = latest.timestamp,
                                    fontSize = 10.sp,
                                    color = MoppiTextTertiary
                                )
                            }
                            Text(
                                text = latest.message,
                                fontSize = 11.sp,
                                color = MoppiTextSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Search Bar & Filter Row
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_anime_input"),
                    placeholder = {
                        Text(
                            text = "Cari judul anime OtakuDesu, genre, atau studio...",
                            fontSize = 13.sp,
                            color = MoppiTextTertiary
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = MoppiTextSecondary
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MoppiWhite,
                        unfocusedContainerColor = MoppiWhite,
                        focusedBorderColor = MoppiSapphire,
                        unfocusedBorderColor = MoppiBorder
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // OtakuDesu Weekly Schedule Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.CalendarMonth,
                            contentDescription = "Jadwal Rilis",
                            tint = MoppiSapphire,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Jadwal Rilis OtakuDesu",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MoppiTextPrimary
                        )
                    }
                    if (selectedScheduleDay != "Semua") {
                        Text(
                            text = "Hari: $selectedScheduleDay",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MoppiSapphire
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Schedule Days Carousel
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(daysList) { day ->
                        val isDaySel = day.equals(selectedScheduleDay, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isDaySel) MoppiCyan else MoppiWhite,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isDaySel) MoppiCyan else MoppiBorder
                            ),
                            modifier = Modifier.clickable { onSelectScheduleDay(day) }
                        ) {
                            Text(
                                text = day,
                                fontSize = 11.sp,
                                fontWeight = if (isDaySel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isDaySel) Color.White else MoppiTextSecondary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Genre Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(displayGenres) { g ->
                        val isSel = g == selectedGenre
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSel) MoppiSapphire else MoppiWhite,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSel) MoppiSapphire else MoppiBorder
                            ),
                            modifier = Modifier.clickable { onGenreSelect(g) }
                        ) {
                            Text(
                                text = g,
                                fontSize = 12.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) MoppiWhite else MoppiTextSecondary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Hero Anime Banner with 1-click Watch & VR Cinema trigger
        if (heroAnime != null && searchQuery.isBlank() && selectedGenre == "All") {
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MoppiWhite,
                    shadowElevation = 3.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MoppiBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onAnimeClick(heroAnime) }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        if (!heroAnime.posterUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = heroAnime.posterUrl,
                                contentDescription = heroAnime.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.moppi_hero_banner),
                                contentDescription = heroAnime.title,
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
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.4f),
                                            Color.Black.copy(alpha = 0.9f)
                                        )
                                    )
                                )
                        )

                        // 22. AUTO UPDATE NEW ANIME TICKER
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MoppiCyan.copy(alpha = 0.95f),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AccessTime,
                                    contentDescription = "Simulcast",
                                    tint = MoppiWhite,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Auto Update Eps Baru: ${heroAnime.nextEpisodeCountdownHours}j lagi",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MoppiWhite
                                )
                            }
                        }

                        // Hero Info & Actions
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "#1 SIMULCAST",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MoppiCyan
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• ${heroAnime.season}",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                            Text(
                                text = heroAnime.title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { onDirectPlay(heroAnime, VideoPlayerMode.NORMAL) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MoppiSapphire),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.PlayArrow,
                                        contentDescription = "Watch",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Nonton (Bebas Iklan)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                OutlinedButton(
                                    onClick = { onDirectPlay(heroAnime, VideoPlayerMode.VR_CINEMA_360) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.8f)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ViewInAr,
                                        contentDescription = "VR",
                                        tint = MoppiCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Mode VR",
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
        }

        // 23. RANKING THE BEST ANIME CAROUSEL
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Leaderboard,
                            contentDescription = "Ranking",
                            tint = MoppiGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "23. Ranking The Best Anime",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MoppiTextPrimary
                        )
                    }
                    Text(
                        text = "Top Airing",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MoppiSapphire
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(topRanked) { anime ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MoppiWhite,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MoppiBorder),
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .width(140.dp)
                                .clickable { onAnimeClick(anime) }
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(MoppiSurfaceSubtle, MoppiSurfaceMuted)
                                            )
                                        )
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

                                    // Rank Number Badge
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(6.dp)
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (anime.rank) {
                                                    1 -> MoppiGold
                                                    2 -> Color(0xFF94A3B8)
                                                    3 -> Color(0xFFD97706)
                                                    else -> MoppiSapphire
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "#${anime.rank}",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 11.sp,
                                            color = MoppiWhite
                                        )
                                    }
                                }

                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = anime.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MoppiTextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "⭐ ${anime.rating}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MoppiGold
                                        )
                                        Text(
                                            text = "${anime.totalEpisodes} Eps",
                                            fontSize = 10.sp,
                                            color = MoppiTextTertiary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 30. LIST ANIME (Full Catalog Grid/List)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "30. List Anime (${filteredAnime.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MoppiTextPrimary
                )
                Text(
                    text = "Status: ${selectedGenre}",
                    fontSize = 11.sp,
                    color = MoppiTextTertiary
                )
            }
        }

        items(filteredAnime) { anime ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MoppiWhite,
                border = androidx.compose.foundation.BorderStroke(1.dp, MoppiBorder),
                shadowElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clickable { onAnimeClick(anime) }
            ) {
                Row(modifier = Modifier.padding(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(width = 85.dp, height = 110.dp)
                            .clip(RoundedCornerShape(10.dp))
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
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = anime.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MoppiTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MoppiSurfaceSubtle
                                ) {
                                    Text(
                                        text = "⭐ ${anime.rating}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MoppiGold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = anime.japaneseTitle,
                                fontSize = 11.sp,
                                color = MoppiTextTertiary,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            // 29. Synopsis Snippet
                            Text(
                                text = anime.synopsis,
                                fontSize = 11.sp,
                                color = MoppiTextSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = anime.genres.take(2).joinToString(" • "),
                                fontSize = 10.sp,
                                color = MoppiSapphire,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${anime.status} • ${anime.studio}",
                                fontSize = 10.sp,
                                color = MoppiTextTertiary
                            )
                        }

                        // OtakuDesu Metadata Badges
                        if (anime.slug != null || anime.dayUpdated != null || anime.batchLink != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (anime.slug != null) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MoppiCyan.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = "⚡ OtakuDesu HD",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MoppiCyan,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                if (anime.dayUpdated != null) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MoppiSapphire.copy(alpha = 0.1f)
                                    ) {
                                        Text(
                                            text = "🗓 ${anime.dayUpdated}",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MoppiSapphire,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                if (anime.batchLink != null) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MoppiViolet.copy(alpha = 0.1f)
                                    ) {
                                        Text(
                                            text = "📦 Batch",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MoppiViolet,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
