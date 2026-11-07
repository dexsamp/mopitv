package com.example.data.repository

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.example.R
import com.example.data.api.AnimeScraperApi
import com.example.data.api.StenlyOtakudesuApi
import com.example.data.api.*
import com.example.data.model.*
import com.example.ui.theme.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MoppiRepository(
    private var scraperApi: AnimeScraperApi = AnimeScraperApi.create(),
    private var stenlyApi: StenlyOtakudesuApi = StenlyOtakudesuApi.create(),
    private var firestoreRepo: WatchPartyFirestoreRepository? = null
) {
    fun initFirestore(context: Context) {
        scraperApi = AnimeScraperApi.create(context)
        stenlyApi = StenlyOtakudesuApi.create(context)
        if (firestoreRepo == null) {
            firestoreRepo = WatchPartyFirestoreRepository.create(context)
        }
    }

    // 1. User Profile State
    private val _userProfile = MutableStateFlow(
        UserProfile(
            id = "user_777",
            username = "MoppiSensei",
            role = UserRole.DEVELOPER,
            isPremium = true,
            moppiGems = 1850,
            equippedFrameId = "frame_galaxy_nebula",
            equippedPetId = "pet_satoru_gojo",
            backgroundGifType = ProfileBackgroundType.CYBER_NEON_GRID,
            warningsCount = 0,
            warningReason = null,
            guildId = "guild_shadow_garden",
            guildName = "Shadow Garden Cyber",
            episodesWatched = 189,
            gachaSpinsCount = 42
        )
    )
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    // 2. Admin Announcements
    private val _announcements = MutableStateFlow(
        listOf(
            AdminAnnouncement(
                id = "ann_1",
                title = "🚀 MoppiTV v3.0 Global Launch!",
                message = "Selamat datang di MoppiTV! Fitur Nobar Sync, Voice Chat, 100+ Anime Pet Gacha, dan VR Cinema Mode resmi aktif. Nikmati streaming tanpa iklan!",
                authorRole = "Developer [System]",
                timestamp = "10 menit yang lalu",
                isUrgent = false
            ),
            AdminAnnouncement(
                id = "ann_2",
                title = "✨ Event Spin Moppi Gem Weekend",
                message = "Drop rate Pet Rarity Galaxy & Mythical meningkat 2x lipat hingga hari Minggu! Kumpulkan Moppi Gem dari quest harian.",
                authorRole = "Admin/Moderator",
                timestamp = "1 jam yang lalu",
                isUrgent = false
            )
        )
    )
    val announcements: StateFlow<List<AdminAnnouncement>> = _announcements.asStateFlow()

    // 3. Anime Catalog & Rankings
    private val _animeList = MutableStateFlow(createInitialAnimeList())
    val animeList: StateFlow<List<Anime>> = _animeList.asStateFlow()

    // 4. Nobar Watch Party Rooms
    private val _nobarRooms = MutableStateFlow(createInitialNobarRooms())
    val nobarRooms: StateFlow<List<NobarRoom>> = _nobarRooms.asStateFlow()

    // 5. Anime Shorts (TikTok-style)
    private val _animeShorts = MutableStateFlow(createInitialShorts())
    val animeShorts: StateFlow<List<AnimeShort>> = _animeShorts.asStateFlow()

    // 6. 100+ Anime Pets Roster
    private val _petsList = MutableStateFlow(create100AnimePets())
    val petsList: StateFlow<List<AnimePet>> = _petsList.asStateFlow()

    // 7. Cool Frames Catalog
    private val _framesList = MutableStateFlow(createCoolFrames())
    val framesList: StateFlow<List<CoolFrame>> = _framesList.asStateFlow()

    // 8. Public Chat Messages
    private val _publicChat = MutableStateFlow(createInitialChat())
    val publicChat: StateFlow<List<NobarChatMessage>> = _publicChat.asStateFlow()

    // 9. Guilds / Squads
    private val _guilds = MutableStateFlow(createInitialGuilds())
    val guilds: StateFlow<List<GuildSquad>> = _guilds.asStateFlow()

    // 10. Achievements
    private val _achievements = MutableStateFlow(createInitialAchievements())
    val achievements: StateFlow<List<Achievement>> = _achievements.asStateFlow()

    // 11. 50 Languages List
    val supportedLanguages: List<SupportedLanguage> = create50Languages()

    // Selected Language
    private val _selectedLanguage = MutableStateFlow(supportedLanguages[0]) // Indonesian default
    val selectedLanguage: StateFlow<SupportedLanguage> = _selectedLanguage.asStateFlow()

    // 12. Stenly Otakudesu Schedule (Senin - Minggu)
    private val _scheduleDays = MutableStateFlow<List<StenlyScheduleDay>>(createDefaultSchedule())
    val scheduleDays: StateFlow<List<StenlyScheduleDay>> = _scheduleDays.asStateFlow()

    private val _selectedScheduleDay = MutableStateFlow("Semua")
    val selectedScheduleDay: StateFlow<String> = _selectedScheduleDay.asStateFlow()

    // 13. Stenly Otakudesu Genres List
    private val _otakuGenres = MutableStateFlow<List<StenlyGenreItem>>(createDefaultGenres())
    val otakuGenres: StateFlow<List<StenlyGenreItem>> = _otakuGenres.asStateFlow()

    // 14. Stenly Otakudesu Batch Downloads Cache
    private val _batchMap = MutableStateFlow<Map<String, StenlyBatchResult>>(emptyMap())
    val batchMap: StateFlow<Map<String, StenlyBatchResult>> = _batchMap.asStateFlow()

    // --- REPOSITORY ACTIONS ---

    fun setLanguage(lang: SupportedLanguage) {
        _selectedLanguage.value = lang
    }

    fun switchUserRole(newRole: UserRole) {
        _userProfile.update { it.copy(role = newRole) }
    }

    fun togglePremium(isPrem: Boolean) {
        _userProfile.update { it.copy(isPremium = isPrem) }
    }

    fun updateProfileBackground(bgType: ProfileBackgroundType) {
        _userProfile.update { it.copy(backgroundGifType = bgType) }
    }

    fun equipPet(petId: String) {
        _userProfile.update { it.copy(equippedPetId = petId) }
        _petsList.update { list ->
            list.map { it.copy(isEquipped = it.id == petId) }
        }
    }

    fun equipFrame(frameId: String) {
        _userProfile.update { it.copy(equippedFrameId = frameId) }
        _framesList.update { list ->
            list.map { it.copy(isEquipped = it.id == frameId) }
        }
    }

    fun spinGacha(isTenPull: Boolean = false): List<AnimePet> {
        val cost = if (isTenPull) 900 else 100
        val currentGems = _userProfile.value.moppiGems
        if (currentGems < cost) return emptyList()

        _userProfile.update {
            it.copy(
                moppiGems = it.moppiGems - cost,
                gachaSpinsCount = it.gachaSpinsCount + if (isTenPull) 10 else 1
            )
        }

        val allPets = _petsList.value
        val pullCount = if (isTenPull) 10 else 1
        val pulledPets = mutableListOf<AnimePet>()

        repeat(pullCount) {
            val randomVal = Math.random()
            val targetRarity = when {
                randomVal < 0.05 -> PetRarity.GALAXY
                randomVal < 0.15 -> PetRarity.MYTHICAL
                randomVal < 0.35 -> PetRarity.LEGEND
                randomVal < 0.60 -> PetRarity.EPIC
                randomVal < 0.85 -> PetRarity.RARE
                else -> PetRarity.COMMON
            }
            val rarityPool = allPets.filter { it.rarity == targetRarity }
            val chosen = if (rarityPool.isNotEmpty()) rarityPool.random() else allPets.random()
            pulledPets.add(chosen)
        }

        // Mark as owned
        val wonIds = pulledPets.map { it.id }.toSet()
        _petsList.update { list ->
            list.map { if (it.id in wonIds) it.copy(isOwned = true) else it }
        }

        return pulledPets
    }

    fun addGems(amount: Int) {
        _userProfile.update { it.copy(moppiGems = it.moppiGems + amount) }
    }

    fun giftGemOrPremium(targetUsername: String, gems: Int, giftPrem: Boolean): Boolean {
        if (gems > 0) {
            if (_userProfile.value.moppiGems < gems) return false
            _userProfile.update { it.copy(moppiGems = it.moppiGems - gems) }
        }
        sendPublicChatMessage("🎁 Gifted $gems Moppi Gems ${if (giftPrem) "& VIP Premium " else ""}to @$targetUsername!")
        return true
    }

    fun sendPublicChatMessage(text: String) {
        val user = _userProfile.value
        val msg = NobarChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            senderName = user.username,
            senderRole = user.role,
            isSenderPremium = user.isPremium,
            message = text,
            timestamp = "Sekarang"
        )
        _publicChat.update { listOf(msg) + it }
    }

    // --- REAL-TIME WATCHPARTY FIRESTORE SYNC ---

    fun observeRoomMessages(roomId: String): Flow<List<NobarChatMessage>> {
        return firestoreRepo?.observeRoomMessages(roomId)
            ?: kotlinx.coroutines.flow.flowOf(createDefaultRoomMessages(roomId))
    }

    suspend fun sendRoomMessage(roomId: String, messageText: String) {
        val user = _userProfile.value
        firestoreRepo?.sendMessage(roomId, messageText, user)
    }

    private fun createDefaultRoomMessages(roomId: String): List<NobarChatMessage> {
        return listOf(
            NobarChatMessage(
                id = "msg_init_1_$roomId",
                senderName = "MoppiBot",
                senderRole = UserRole.DEVELOPER,
                isSenderPremium = true,
                senderUid = "bot_01",
                senderAvatarEmoji = "👑",
                message = "Selamat datang di WatchParty room! Stream tersinkronisasi detik demi detik 🔥",
                timestamp = "12:00"
            ),
            NobarChatMessage(
                id = "msg_init_2_$roomId",
                senderName = "SepuhAnime",
                senderRole = UserRole.SEPUH,
                isSenderPremium = true,
                senderUid = "sepuh_01",
                senderAvatarEmoji = "⚡",
                message = "Kualitas stream mantap, audio chat jernih banget! 🍿",
                timestamp = "12:02"
            )
        )
    }

    fun createNobarRoom(title: String, animeTitle: String, isPrivate: Boolean, pin: String?): NobarRoom {
        val user = _userProfile.value
        val matchingAnime = _animeList.value.find { it.title == animeTitle }
        val newRoom = NobarRoom(
            id = "room_${System.currentTimeMillis()}",
            title = title,
            animeTitle = animeTitle,
            episodeNumber = 1,
            hostUsername = user.username,
            hostRole = user.role,
            isPrivate = isPrivate,
            pinCode = pin,
            participantCount = 1,
            isVoiceEnabled = true,
            thumbnailUrl = matchingAnime?.posterUrl ?: "https://cdn.myanimelist.net/images/anime/1170/124312l.jpg",
            videoUrl = matchingAnime?.episodes?.firstOrNull()?.videoUrl ?: matchingAnime?.trailerUrl ?: "https://www.youtube.com/watch?v=91t0qWfLgQc"
        )
        _nobarRooms.update { listOf(newRoom) + it }
        return newRoom
    }

    /**
     * Scrapes real anime titles, Japanese titles, synopsis, official posters/thumbnails,
     * ratings, and trailers directly from Jikan v4 (Official MyAnimeList scraper).
     */
    suspend fun fetchAnimeFromScraper() {
        try {
            val response = scraperApi.getTopAnime(limit = 20)
            val scrapedData = response.data
            if (!scrapedData.isNullOrEmpty()) {
                val scrapedAnimeList = scrapedData.mapIndexed { idx, item ->
                    val poster = item.images?.webp?.largeImageUrl
                        ?: item.images?.jpg?.largeImageUrl
                        ?: item.images?.webp?.imageUrl
                        ?: item.images?.jpg?.imageUrl
                        ?: "https://cdn.myanimelist.net/images/anime/1170/124312l.jpg"

                    val trailerUrl = item.trailer?.url ?: item.trailer?.embedUrl ?: "https://www.youtube.com/watch?v=91t0qWfLgQc"
                    val genres = item.genres?.mapNotNull { it.name } ?: listOf("Action", "Adventure")
                    val epCount = item.episodes ?: 12
                    val score = item.score ?: 8.5
                    val studio = item.studios?.firstOrNull()?.name ?: "Anime Studio"

                    val videoStream = trailerUrl

                    Anime(
                        id = "scraped_${item.malId ?: idx}",
                        title = item.title ?: item.titleEnglish ?: "Anime Series",
                        japaneseTitle = item.titleJapanese ?: "アニメ",
                        synopsis = item.synopsis ?: "Anime synopsis with exciting story and epic battles.",
                        genres = genres,
                        rating = score,
                        rank = item.rank ?: (idx + 1),
                        season = "${item.season?.replaceFirstChar { it.uppercase() } ?: "Fall"} ${item.year ?: 2026}",
                        studio = studio,
                        totalEpisodes = epCount,
                        status = item.status ?: "Ongoing",
                        isSimulcast = true,
                        nextEpisodeCountdownHours = 12 + idx * 4,
                        posterUrl = poster,
                        trailerUrl = trailerUrl,
                        bannerDrawableRes = R.drawable.moppi_hero_banner,
                        episodes = (1..epCount.coerceAtMost(12)).map { ep ->
                            Episode(
                                episodeNumber = ep,
                                title = "Episode $ep: Clash of Destinies",
                                duration = "24:15",
                                introStartSec = 80,
                                introEndSec = 170,
                                thumbnailUrl = poster,
                                videoUrl = videoStream
                            )
                        }
                    )
                }

                // Update anime list with real scraped titles & posters
                _animeList.update { current ->
                    (scrapedAnimeList + current).distinctBy { it.title }
                }

                // Also update anime shorts reels with real thumbnails
                val newShorts = scrapedData.take(8).mapIndexed { idx, item ->
                    val poster = item.images?.webp?.largeImageUrl
                        ?: item.images?.jpg?.largeImageUrl
                        ?: "https://cdn.myanimelist.net/images/anime/1170/124312l.jpg"

                    AnimeShort(
                        id = "short_scraped_${idx}_${System.currentTimeMillis()}",
                        title = "${item.title ?: "Anime"} - Epic Scene HD! 🔥",
                        animeSource = item.title ?: "Anime Series",
                        uploaderName = "MoppiClipper",
                        uploaderRole = if (idx == 0) UserRole.DEVELOPER else UserRole.SEPUH,
                        likesCount = 28000 + idx * 1200,
                        commentsCount = 650 + idx * 80,
                        sharesCount = 1100 + idx * 90,
                        videoStreamUrl = item.trailer?.url ?: item.trailer?.embedUrl ?: "https://www.youtube.com/watch?v=91t0qWfLgQc",
                        thumbnailUrl = poster
                    )
                }
                _animeShorts.update { (newShorts + it).distinctBy { s -> s.title } }
            }
        } catch (e: Exception) {
            // Keep verified high-resolution cached catalog
        }

        // Simultaneously fetch Stenly Otakudesu Home, Schedule, and Genres
        fetchOtakudesuData()
    }

    /**
     * Fetches Home (Ongoing & Completed), Schedule, and Genres from Stenly API Hub Otakudesu:
     * https://stenly.co/stenlyapi-hub/api/v1/otakudesu
     */
    suspend fun fetchOtakudesuData() {
        try {
            // 1. Home Endpoint: https://stenly.co/stenlyapi-hub/api/v1/otakudesu
            val homeRes = stenlyApi.getHome()
            val ongoing = homeRes.result?.ongoing ?: emptyList()
            val complete = homeRes.result?.complete ?: emptyList()

            val combined = (ongoing + complete).distinctBy { it.title }
            if (combined.isNotEmpty()) {
                val otakudesuAnime = combined.mapIndexed { idx, item ->
                    val slug = item.cleanSlug.ifBlank { "otaku_${idx}" }
                    Anime(
                        id = "otaku_$slug",
                        title = item.title ?: "Anime Sub Indo",
                        japaneseTitle = "アニメ • ${item.episode ?: "Ongoing"}",
                        synopsis = "Tonton streaming anime ${item.title} subtitle Indonesia kualitas HD terbaru di MoppiTV.",
                        genres = item.genres ?: listOf("Action", "Fantasy", "Animation"),
                        rating = item.rating?.toDoubleOrNull() ?: (8.4 + (idx % 10) * 0.1),
                        rank = idx + 1,
                        season = "Ongoing 2026",
                        studio = item.studio ?: "OtakuDesu Stream",
                        totalEpisodes = item.episode?.filter { it.isDigit() }?.toIntOrNull() ?: 12,
                        status = if (ongoing.contains(item)) "Ongoing" else "Completed",
                        isSimulcast = true,
                        posterUrl = item.displayPoster,
                        trailerUrl = null,
                        bannerDrawableRes = R.drawable.moppi_hero_banner,
                        slug = slug,
                        dayUpdated = item.dayOrRating,
                        episodes = (1..6).map { ep ->
                            Episode(
                                episodeNumber = ep,
                                title = "Episode $ep Sub Indo",
                                duration = "24:00",
                                videoUrl = "",
                                slug = null // Populated dynamically upon detail fetch
                            )
                        }
                    )
                }

                _animeList.update { current ->
                    (otakudesuAnime + current).distinctBy { it.title }
                }
            }
        } catch (e: Exception) {
            // graceful fallback
        }

        try {
            // 2. Schedule Endpoint: https://stenly.co/stenlyapi-hub/api/v1/otakudesu?type=schedule
            val scheduleRes = stenlyApi.getSchedule("schedule")
            val days = scheduleRes.result
            if (!days.isNullOrEmpty()) {
                _scheduleDays.value = days
            }
        } catch (e: Exception) {
            // graceful fallback
        }

        try {
            // 3. Genres Endpoint: https://stenly.co/stenlyapi-hub/api/v1/otakudesu?type=genres
            val genreRes = stenlyApi.getGenresList("genres")
            val genres = genreRes.result
            if (!genres.isNullOrEmpty()) {
                _otakuGenres.value = genres
            }
        } catch (e: Exception) {
            // graceful fallback
        }
    }

    /**
     * Detail Endpoint: Fetches anime synopsis, genres, batch info, and full episode list
     * https://stenly.co/stenlyapi-hub/api/v1/otakudesu/detail?slug=...
     */
    suspend fun fetchAnimeDetail(animeSlug: String): StenlyDetailResult? {
        return try {
            val res = stenlyApi.getAnimeDetail(animeSlug)
            val detail = res.result
            if (detail != null) {
                _animeList.update { list ->
                    list.map { a ->
                        if (a.slug == animeSlug || a.title.equals(detail.title, ignoreCase = true)) {
                            val eps = detail.episodes?.mapIndexed { idx, ep ->
                                Episode(
                                    episodeNumber = idx + 1,
                                    title = ep.title ?: "Episode ${idx + 1}",
                                    duration = detail.duration ?: "24:00",
                                    slug = ep.cleanSlug,
                                    videoUrl = ""
                                )
                            } ?: a.episodes
                            a.copy(
                                synopsis = detail.synopsis ?: a.synopsis,
                                japaneseTitle = detail.japanese ?: a.japaneseTitle,
                                studio = detail.studio ?: detail.producer ?: a.studio,
                                genres = detail.genres ?: a.genres,
                                batchLink = detail.batch,
                                episodes = eps
                            )
                        } else a
                    }
                }
            }
            detail
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Watch Endpoint: Retrieves real video player stream (mp4 direct stream, HLS proxy, or responsive embed player)
     * https://stenly.co/stenlyapi-hub/api/v1/otakudesu/watch?slug=...
     */
    suspend fun fetchEpisodeWatchStream(episodeSlug: String): String? {
        return try {
            val watchRes = stenlyApi.getWatchEpisode(episodeSlug)
            watchRes.result?.bestVideoUrl
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Search Endpoint: Searches Otakudesu anime catalog
     * https://stenly.co/stenlyapi-hub/api/v1/otakudesu?q=...
     */
    suspend fun searchAnimeOtakudesu(query: String) {
        if (query.isBlank()) return
        try {
            val res = stenlyApi.searchAnime(query)
            val list = res.result ?: emptyList()
            if (list.isNotEmpty()) {
                val mapped = list.mapIndexed { idx, item ->
                    Anime(
                        id = "search_${item.cleanSlug}_$idx",
                        title = item.title ?: query,
                        japaneseTitle = "アニメ",
                        synopsis = "Hasil pencarian OtakuDesu untuk $query",
                        genres = item.genres ?: listOf("Action", "Fantasy"),
                        rating = item.rating?.toDoubleOrNull() ?: 8.5,
                        rank = idx + 1,
                        season = "2026",
                        studio = item.studio ?: "OtakuDesu",
                        totalEpisodes = 12,
                        status = item.status ?: "Ongoing",
                        posterUrl = item.displayPoster,
                        trailerUrl = "https://www.youtube.com/watch?v=91t0qWfLgQc",
                        slug = item.cleanSlug
                    )
                }
                _animeList.update { (mapped + it).distinctBy { a -> a.title } }
            }
        } catch (e: Exception) {
            // graceful fallback
        }
    }

    /**
     * Genres Filter Endpoint: Filters anime by specific genre
     * https://stenly.co/stenlyapi-hub/api/v1/otakudesu?genre=...&page=1
     */
    suspend fun filterByGenre(genreSlug: String) {
        try {
            val res = stenlyApi.getAnimeByGenre(genreSlug, 1)
            val list = res.result ?: emptyList()
            if (list.isNotEmpty()) {
                val mapped = list.mapIndexed { idx, item ->
                    Anime(
                        id = "genre_${item.cleanSlug}_$idx",
                        title = item.title ?: "Anime Genre",
                        japaneseTitle = "アニメ",
                        synopsis = "Anime genre $genreSlug dari OtakuDesu",
                        genres = item.genres ?: listOf(genreSlug.replaceFirstChar { it.uppercase() }),
                        rating = item.rating?.toDoubleOrNull() ?: 8.5,
                        rank = idx + 1,
                        season = "2026",
                        studio = item.studio ?: "OtakuDesu",
                        totalEpisodes = 12,
                        status = item.status ?: "Ongoing",
                        posterUrl = item.displayPoster,
                        trailerUrl = "https://www.youtube.com/watch?v=91t0qWfLgQc",
                        slug = item.cleanSlug
                    )
                }
                _animeList.update { (mapped + it).distinctBy { a -> a.title } }
            }
        } catch (e: Exception) {
            // graceful fallback
        }
    }

    /**
     * Batch Endpoint: Retrieves batch download & stream packages
     * https://stenly.co/stenlyapi-hub/api/v1/otakudesu?batch=...
     */
    suspend fun fetchBatch(batchSlug: String): StenlyBatchResult? {
        return try {
            val res = stenlyApi.getBatch(batchSlug)
            val batch = res.result
            if (batch != null) {
                _batchMap.update { it + (batchSlug to batch) }
            }
            batch
        } catch (e: Exception) {
            null
        }
    }

    fun selectScheduleDay(day: String) {
        _selectedScheduleDay.value = day
    }

    fun toggleShortLike(shortId: String) {
        _animeShorts.update { list ->
            list.map {
                if (it.id == shortId) {
                    val newLiked = !it.isLiked
                    it.copy(
                        isLiked = newLiked,
                        likesCount = if (newLiked) it.likesCount + 1 else it.likesCount - 1
                    )
                } else it
            }
        }
    }

    fun claimAchievement(id: String) {
        _achievements.update { list ->
            list.map {
                if (it.id == id && !it.isClaimed && it.currentProgress >= it.targetProgress) {
                    addGems(it.gemReward)
                    it.copy(isClaimed = true)
                } else it
            }
        }
    }

    fun joinGuild(guildId: String) {
        _guilds.update { list ->
            list.map { it.copy(isJoined = it.id == guildId) }
        }
        val joined = _guilds.value.find { it.id == guildId }
        _userProfile.update { it.copy(guildId = guildId, guildName = joined?.name) }
    }

    // Admin Tools
    fun publishAnnouncement(title: String, message: String, isUrgent: Boolean) {
        val ann = AdminAnnouncement(
            id = "ann_${System.currentTimeMillis()}",
            title = title,
            message = message,
            authorRole = "${_userProfile.value.role.title} [System]",
            timestamp = "Baru saja",
            isUrgent = isUrgent
        )
        _announcements.update { listOf(ann) + it }
    }

    fun issueSystemWarn(targetUser: String, reason: String) {
        if (targetUser == _userProfile.value.username) {
            _userProfile.update {
                it.copy(
                    warningsCount = it.warningsCount + 1,
                    warningReason = reason
                )
            }
        }
        sendPublicChatMessage("⚠️ [SYSTEM WARN] @$targetUser menerima peringatan resmi: $reason")
    }

    // --- INITIAL DATA FACTORIES ---

    private fun createInitialAnimeList(): List<Anime> {
        return listOf(
            Anime(
                id = "anime_uchi_otouto",
                title = "Uchi no Otouto-domo ga Sumimasen",
                japaneseTitle = "うちの弟どもがすみません • Eps 18",
                synopsis = "Tonton streaming anime Uchi no Otouto-domo ga Sumimasen subtitle Indonesia kualitas HD terbaru di MoppiTV.",
                genres = listOf("Comedy", "Romance", "Slice of Life"),
                rating = 9.2,
                rank = 1,
                season = "Ongoing 2026",
                studio = "OtakuDesu Stream",
                totalEpisodes = 18,
                status = "Ongoing",
                isSimulcast = true,
                nextEpisodeCountdownHours = 6,
                posterUrl = "https://otakudesu.blog/wp-content/uploads/2026/07/Uchi-no-Otouto-domo-ga-Sumimasen.jpg",
                trailerUrl = null,
                bannerDrawableRes = R.drawable.moppi_hero_banner,
                slug = "uchi-otouto-sumimasen-sub-indo",
                episodes = (18 downTo 1).map { ep ->
                    Episode(
                        episodeNumber = ep,
                        title = "Episode $ep Sub Indo",
                        duration = "24:00",
                        videoUrl = if (ep == 1) "https://www.blogger.com/video.g?token=AD6v5dyLN-LhK4bgNpX3bhOMFdpZiFk_1jQ_AvoQTDp6JRjCTcHrLwNFv4XCu3sfN7aWAYV1f-0gAP9hR17Nnonh3f9uTc3qeFd29QmhdTxPbcfZt1ZfTNSB6ZRK6_GysENjeDhF-Ojb&origin=kcnwnuewnuwiwniejiscnwdesugoing.blogspot.com" else "",
                        slug = "utgs-episode-$ep-sub-indo"
                    )
                }
            ),
            Anime(
                id = "anime_tougen_anki",
                title = "Tougen Anki: Nikko Kegon no Taki-hen",
                japaneseTitle = "桃源暗鬼 • Ongoing",
                synopsis = "Pertarungan berdarah antara klan Oni dan Momotaro berlanjut di Air Terjun Nikko Kegon.",
                genres = listOf("Action", "Supernatural", "Fantasy"),
                rating = 9.1,
                rank = 2,
                season = "Ongoing 2026",
                studio = "OtakuDesu Stream",
                totalEpisodes = 6,
                status = "Ongoing",
                isSimulcast = true,
                nextEpisodeCountdownHours = 12,
                posterUrl = "https://otakudesu.blog/wp-content/uploads/2026/07/Tougen-Anki-Nikko-Kegon-no-Taki-hen.jpg",
                trailerUrl = null,
                bannerDrawableRes = R.drawable.moppi_hero_banner,
                slug = "tougen-nikko-kegon-sub-indo",
                episodes = (6 downTo 1).map { ep ->
                    Episode(
                        episodeNumber = ep,
                        title = "Episode $ep Sub Indo",
                        duration = "23:45",
                        videoUrl = "",
                        slug = "tnknt-episode-$ep-sub-indo"
                    )
                }
            ),
            Anime(
                id = "anime_hotaru_yomeiri",
                title = "Hotaru no Yomeiri",
                japaneseTitle = "ホタルの嫁入り • Ongoing",
                synopsis = "Kisah romansa misteri dan pernikahan berbahaya di era Meiji antara putri bangsawan dan pembunuh bayaran.",
                genres = listOf("Drama", "Historical", "Romance"),
                rating = 9.0,
                rank = 3,
                season = "Ongoing 2026",
                studio = "OtakuDesu Stream",
                totalEpisodes = 5,
                status = "Ongoing",
                isSimulcast = true,
                nextEpisodeCountdownHours = 18,
                posterUrl = "https://otakudesu.blog/wp-content/uploads/2026/07/Hotaru-no-Yomeiri.jpg",
                trailerUrl = null,
                bannerDrawableRes = R.drawable.moppi_hero_banner,
                slug = "hotaru-yomeiri-sub-indo",
                episodes = (5 downTo 1).map { ep ->
                    Episode(
                        episodeNumber = ep,
                        title = "Episode $ep Sub Indo",
                        duration = "24:10",
                        videoUrl = "",
                        slug = "hnyi-episode-$ep-sub-indo"
                    )
                }
            ),
            Anime(
                id = "anime_tokyo_revengers",
                title = "Tokyo Revengers Season 4",
                japaneseTitle = "東京リベンジャーズ • Ongoing",
                synopsis = "Takemichi kembali melompati waktu untuk menyelamatkan masa depan geng Tokyo Manji dari ancaman baru.",
                genres = listOf("Action", "Drama", "Supernatural"),
                rating = 8.95,
                rank = 4,
                season = "Ongoing 2026",
                studio = "LIDENFILMS",
                totalEpisodes = 6,
                status = "Ongoing",
                isSimulcast = true,
                nextEpisodeCountdownHours = 24,
                posterUrl = "https://otakudesu.blog/wp-content/uploads/2026/07/Tokyo-Revengers-Season-4.jpg",
                trailerUrl = null,
                bannerDrawableRes = R.drawable.moppi_hero_banner,
                slug = "tokyo-revs-sanhen-sub-indo",
                episodes = (6 downTo 1).map { ep ->
                    Episode(
                        episodeNumber = ep,
                        title = "Episode $ep Sub Indo",
                        duration = "24:00",
                        videoUrl = "",
                        slug = if (ep == 6) "tkrvgs-s4-episode-6-sub-indo-2" else "tkrvgs-s4-episode-$ep-sub-indo"
                    )
                }
            ),
            Anime(
                id = "anime_kusuriya",
                title = "Kusuriya no Hitorigoto Season 3",
                japaneseTitle = "薬屋のひとりごと • Ongoing",
                synopsis = "Maomao terus memecahkan misteri racun dan intrik politik di istana kekaisaran dengan keahlian farmasinya yang brilian.",
                genres = listOf("Drama", "Mystery", "Historical"),
                rating = 9.3,
                rank = 5,
                season = "Ongoing 2026",
                studio = "TOHO animation / OLM",
                totalEpisodes = 6,
                status = "Ongoing",
                isSimulcast = true,
                nextEpisodeCountdownHours = 32,
                posterUrl = "https://otakudesu.blog/wp-content/uploads/2026/07/Kusuriya-no-Hitorigoto-Season-3.jpg",
                trailerUrl = null,
                bannerDrawableRes = R.drawable.moppi_hero_banner,
                slug = "kusuriya-hitorigoto-s3-sub-indo",
                episodes = (6 downTo 1).map { ep ->
                    Episode(
                        episodeNumber = ep,
                        title = "Episode $ep Sub Indo",
                        duration = "23:55",
                        videoUrl = "",
                        slug = "knh-s3-episode-$ep-sub-indo"
                    )
                }
            )
        )
    }

    private fun createInitialNobarRooms(): List<NobarRoom> {
        return listOf(
            NobarRoom(
                id = "room_1",
                title = "🔥 Nobar Uchi no Otouto Eps 18 [Voice ON]",
                animeTitle = "Uchi no Otouto-domo ga Sumimasen",
                episodeNumber = 18,
                hostUsername = "ShadowMaster",
                hostRole = UserRole.DEVELOPER,
                isPrivate = false,
                participantCount = 38,
                maxParticipants = 50,
                isVoiceEnabled = true,
                currentTimestampSeconds = 480,
                thumbnailUrl = "https://otakudesu.blog/wp-content/uploads/2026/07/Uchi-no-Otouto-domo-ga-Sumimasen.jpg",
                videoUrl = ""
            ),
            NobarRoom(
                id = "room_2",
                title = "🍿 Tougen Anki Squad",
                animeTitle = "Tougen Anki: Nikko Kegon no Taki-hen",
                episodeNumber = 1,
                hostUsername = "GojoFanboy",
                hostRole = UserRole.SEPUH,
                isPrivate = false,
                participantCount = 22,
                maxParticipants = 30,
                isVoiceEnabled = true,
                currentTimestampSeconds = 720,
                thumbnailUrl = "https://otakudesu.blog/wp-content/uploads/2026/07/Tougen-Anki-Nikko-Kegon-no-Taki-hen.jpg",
                videoUrl = ""
            )
        )
    }

    private fun createInitialShorts(): List<AnimeShort> {
        return listOf(
            AnimeShort(
                id = "short_1",
                title = "Sung Jinwoo summon Igris cutscene HD! 🔥",
                animeSource = "Solo Leveling: Arise",
                uploaderName = "AriseClipper",
                uploaderRole = UserRole.SEPUH,
                likesCount = 24500,
                commentsCount = 890,
                sharesCount = 1240,
                thumbnailUrl = "https://cdn.myanimelist.net/images/anime/1170/124312l.jpg",
                videoStreamUrl = "https://www.youtube.com/watch?v=91t0qWfLgQc",
                gradientColors = listOf(0xFF1E1B4B, 0xFF312E81)
            ),
            AnimeShort(
                id = "short_2",
                title = "Gojo Satoru Infinite Void Domain Expansion! 🌀",
                animeSource = "Jujutsu Kaisen",
                uploaderName = "JJK_God",
                uploaderRole = UserRole.DEVELOPER,
                likesCount = 48900,
                commentsCount = 2100,
                sharesCount = 4500,
                thumbnailUrl = "https://cdn.myanimelist.net/images/anime/1171/109222l.jpg",
                videoStreamUrl = "https://www.youtube.com/watch?v=pkKu9hLT-t8",
                gradientColors = listOf(0xFF0F172A, 0xFF1E293B)
            ),
            AnimeShort(
                id = "short_3",
                title = "Frieren casting Zoltraak spell instantly ✨",
                animeSource = "Frieren: Beyond Journey's End",
                uploaderName = "ElvenSpells",
                uploaderRole = UserRole.WARGA,
                likesCount = 19200,
                commentsCount = 670,
                sharesCount = 890,
                thumbnailUrl = "https://cdn.myanimelist.net/images/anime/1015/138075l.jpg",
                videoStreamUrl = "https://www.youtube.com/watch?v=qgQunxD0qmo",
                gradientColors = listOf(0xFF3B0764, 0xFF581C87)
            ),
            AnimeShort(
                id = "short_4",
                title = "I Am Atomic! Cid coolest scene ever 💥",
                animeSource = "The Eminence in Shadow",
                uploaderName = "LordShadow",
                uploaderRole = UserRole.ADMIN,
                likesCount = 31200,
                commentsCount = 1430,
                sharesCount = 3200,
                thumbnailUrl = "https://cdn.myanimelist.net/images/anime/1874/121869l.jpg",
                videoStreamUrl = "https://www.youtube.com/watch?v=0kF3h_27jJk",
                gradientColors = listOf(0xFF18181B, 0xFF27272A)
            )
        )
    }

    private fun createCoolFrames(): List<CoolFrame> {
        return listOf(
            CoolFrame(
                id = "frame_galaxy_nebula",
                name = "Galaxy Nebula Hologram",
                description = "Bingkai kosmik bercahaya ungu-pink hologram dengan efek bintang bergerak.",
                gradientColors = listOf(Color(0xFFD946EF), Color(0xFF8B5CF6), Color(0xFF06B6D4)),
                glowColor = Color(0xFFD946EF),
                isOwned = true,
                isEquipped = true,
                gemCost = 350
            ),
            CoolFrame(
                id = "frame_cyber_sapphire",
                name = "Cyberpunk Sapphire",
                description = "Bingkai sirkuit elektrik canggih warna biru sapphire modern.",
                gradientColors = listOf(Color(0xFF2563EB), Color(0xFF38BDF8), Color(0xFF0284C7)),
                glowColor = Color(0xFF38BDF8),
                isOwned = true,
                gemCost = 250
            ),
            CoolFrame(
                id = "frame_flame_dragon",
                name = "Mythical Dragon Flames",
                description = "Aura kobaran api naga merah keemasan legendaris.",
                gradientColors = listOf(Color(0xFFEF4444), Color(0xFFF59E0B), Color(0xFFDC2626)),
                glowColor = Color(0xFFEF4444),
                isOwned = false,
                gemCost = 450
            ),
            CoolFrame(
                id = "frame_matrix_glitch",
                name = "Matrix Code Glitch",
                description = "Gaya cyberpunk glitch neon hijau matriks digital.",
                gradientColors = listOf(Color(0xFF10B981), Color(0xFF059669), Color(0xFF34D399)),
                glowColor = Color(0xFF10B981),
                isOwned = false,
                gemCost = 300
            ),
            CoolFrame(
                id = "frame_sakura_blossom",
                name = "Sakura Spirit Aura",
                description = "Kelopak bunga sakura bercahaya pastel lembut minimalis.",
                gradientColors = listOf(Color(0xFFF472B6), Color(0xFFFB7185), Color(0xFFFDA4AF)),
                glowColor = Color(0xFFF472B6),
                isOwned = true,
                gemCost = 200
            )
        )
    }

    private fun createInitialChat(): List<NobarChatMessage> {
        return listOf(
            NobarChatMessage(
                id = "chat_1",
                senderName = "MoppiMaster",
                senderRole = UserRole.DEVELOPER,
                isSenderPremium = true,
                message = "Selamat datang di MoppiTV! Laporkan bug langsung ke dev.",
                timestamp = "12:01"
            ),
            NobarChatMessage(
                id = "chat_2",
                senderName = "WibuSepuh_99",
                senderRole = UserRole.SEPUH,
                isSenderPremium = true,
                message = "Ada yang mau nobar Jujutsu Kaisen di room 2? Voice chat nyala ya!",
                timestamp = "12:03"
            ),
            NobarChatMessage(
                id = "chat_3",
                senderName = "KiraAdmin",
                senderRole = UserRole.ADMIN,
                isSenderPremium = true,
                message = "Harap patuhi tata tertib komunitas. Dilarang spam!",
                timestamp = "12:04"
            ),
            NobarChatMessage(
                id = "chat_4",
                senderName = "BudiAnime",
                senderRole = UserRole.WARGA,
                isSenderPremium = false,
                message = "Gila gacha 10x tadi dapet Gojo Satoru Galaxy Rarity! Hoki banget! 🎉",
                timestamp = "12:05"
            )
        )
    }

    private fun createInitialGuilds(): List<GuildSquad> {
        return listOf(
            GuildSquad(
                id = "guild_shadow_garden",
                name = "Shadow Garden Cyber",
                tag = "SDW",
                leaderName = "LordShadow",
                memberCount = 28,
                maxMembers = 30,
                rankPosition = 1,
                trophyScore = 18450,
                slogan = "Bergerak dalam bayangan, memerintah kebenaran.",
                isJoined = true
            ),
            GuildSquad(
                id = "guild_akatsuki",
                name = "Akatsuki Vanguard",
                tag = "AKT",
                leaderName = "Pain_Deva",
                memberCount = 25,
                maxMembers = 30,
                rankPosition = 2,
                trophyScore = 16200,
                slogan = "Keadilan sejati lewat kedamaian mutlak.",
                isJoined = false
            ),
            GuildSquad(
                id = "guild_survey_corps",
                name = "Survey Corps Wings",
                tag = "SCW",
                leaderName = "ErwinSmith",
                memberCount = 29,
                maxMembers = 30,
                rankPosition = 3,
                trophyScore = 14900,
                slogan = "Sasageyo! Bebaskan dunia dari tirani.",
                isJoined = false
            )
        )
    }

    private fun createInitialAchievements(): List<Achievement> {
        return listOf(
            Achievement(
                id = "ach_watch_10",
                title = "Anime Connoisseur I",
                description = "Tonton total 10 episode anime apa saja.",
                currentProgress = 10,
                targetProgress = 10,
                gemReward = 150,
                isClaimed = true
            ),
            Achievement(
                id = "ach_spin_5",
                title = "Gacha Enthusiast",
                description = "Lakukan 5x spin di Pet Companion / Bingkai gacha.",
                currentProgress = 5,
                targetProgress = 5,
                gemReward = 200,
                isClaimed = false
            ),
            Achievement(
                id = "ach_nobar_join",
                title = "Party Watcher",
                description = "Ikuti atau buat 1 room nobar bersama teman.",
                currentProgress = 1,
                targetProgress = 1,
                gemReward = 100,
                isClaimed = false
            ),
            Achievement(
                id = "ach_guild_join",
                title = "Squad Brotherhood",
                description = "Bergabung ke dalam Guild / Squad anime.",
                currentProgress = 1,
                targetProgress = 1,
                gemReward = 250,
                isClaimed = true
            ),
            Achievement(
                id = "ach_galaxy_pet",
                title = "Celestial Master",
                description = "Miliki minimal 1 Anime Pet berperingkat Galaxy.",
                currentProgress = 1,
                targetProgress = 1,
                gemReward = 500,
                isClaimed = false
            )
        )
    }

    // 100+ Anime Pet Characters Roster (Mythical, Legend, Galaxy, Epic, Rare, Common)
    private fun create100AnimePets(): List<AnimePet> {
        val pets = mutableListOf<AnimePet>()

        // 1. Galaxy Rarity (Celestial Tier - 1% drop rate)
        val galaxyData = listOf(
            Triple("pet_satoru_gojo", "Gojo Satoru (Limitless)", "Jujutsu Kaisen"),
            Triple("pet_sung_jinwoo", "Sung Jin-woo (Shadow Monarch)", "Solo Leveling"),
            Triple("pet_rimuru_tempest", "Rimuru Tempest (Demon Lord)", "Slime Isekai"),
            Triple("pet_gear5_luffy", "Monkey D. Luffy (Gear 5 Nika)", "One Piece"),
            Triple("pet_frieren_celestial", "Frieren (Ancient Mage)", "Frieren"),
            Triple("pet_cid_shadow", "Shadow (Cid Kagenou)", "Eminence in Shadow"),
            Triple("pet_madara_sixpaths", "Madara Uchiha (Rikudo)", "Naruto Shippuden"),
            Triple("pet_saitama_serious", "Saitama (Serious Mode)", "One Punch Man"),
            Triple("pet_aizen_transcendent", "Sosuke Aizen (Hokyoku)", "Bleach"),
            Triple("pet_sukuna_heian", "Ryomen Sukuna (Heian Form)", "Jujutsu Kaisen")
        )
        galaxyData.forEachIndexed { i, d ->
            pets.add(
                AnimePet(
                    id = d.first,
                    name = d.second,
                    animeOrigin = d.third,
                    rarity = PetRarity.GALAXY,
                    quote = "Aku adalah yang terkuat di semesta ini!",
                    powerBonus = "+50% EXP Stream & Aura Bintang",
                    iconEmoji = "🌌",
                    isOwned = i < 2,
                    isEquipped = i == 0
                )
            )
        }

        // 2. Mythical Rarity (4% drop rate)
        val mythicalData = listOf(
            Triple("pet_naruto_baryon", "Naruto Uzumaki (Baryon Mode)", "Naruto"),
            Triple("pet_sasuke_rinnegan", "Sasuke Uchiha (Rinnegan)", "Naruto"),
            Triple("pet_eren_founding", "Eren Yeager (Founding Titan)", "Attack on Titan"),
            Triple("pet_levi_ackerman", "Levi Ackerman (Humanity's Strongest)", "Attack on Titan"),
            Triple("pet_tanjiro_sun", "Tanjiro Kamado (Sun Breathing)", "Demon Slayer"),
            Triple("pet_ichigo_mugetsu", "Ichigo Kurosaki (Mugetsu Final)", "Bleach"),
            Triple("pet_alucard_hellsing", "Alucard (Level Zero)", "Hellsing Ultimate"),
            Triple("pet_mob_100", "Shigeo Kageyama (Mob 100%)", "Mob Psycho 100"),
            Triple("pet_gon_adult", "Gon Freecss (Adult Vow)", "Hunter x Hunter"),
            Triple("pet_killua_godspeed", "Killua Zoldyck (Godspeed)", "Hunter x Hunter"),
            Triple("pet_rengoku_kyojuro", "Kyojuro Rengoku (Flame Hashira)", "Demon Slayer"),
            Triple("pet_zoro_kingofhell", "Roronoa Zoro (King of Hell)", "One Piece"),
            Triple("pet_whitebeard_prime", "Edward Newgate (Whitebeard)", "One Piece"),
            Triple("pet_giorno_ger", "Giorno Giovanna (GER)", "JoJo Bizarre Adv"),
            Triple("pet_dio_world", "DIO (The World Over Heaven)", "JoJo Bizarre Adv")
        )
        mythicalData.forEachIndexed { i, d ->
            pets.add(
                AnimePet(
                    id = d.first,
                    name = d.second,
                    animeOrigin = d.third,
                    rarity = PetRarity.MYTHICAL,
                    quote = "Setiap langkah akan menentukan takdir pertarungan!",
                    powerBonus = "+35% Drop Rate Koin Moppi",
                    iconEmoji = "🔥",
                    isOwned = i < 3
                )
            )
        }

        // 3. Legend Rarity (10% drop rate)
        val legendData = listOf(
            Triple("pet_anya_forger", "Anya Forger (Telepath)", "Spy x Family"),
            Triple("pet_nezuko_kamado", "Nezuko Kamado (Awakened)", "Demon Slayer"),
            Triple("pet_igris_shadow", "Blood-Red Commander Igris", "Solo Leveling"),
            Triple("pet_beru_antking", "Beru (Ant King Shadow)", "Solo Leveling"),
            Triple("pet_milim_nava", "Milim Nava (Dragonoid)", "Slime Isekai"),
            Triple("pet_diablo_black", "Diablo (Noir Primordial)", "Slime Isekai"),
            Triple("pet_kakashi_anbu", "Kakashi Hatake (Lightning Blade)", "Naruto"),
            Triple("pet_itachi_susanoo", "Itachi Uchiha (Tsukuyomi)", "Naruto"),
            Triple("pet_trafalgar_law", "Trafalgar D. Water Law (Room)", "One Piece"),
            Triple("pet_portgas_ace", "Portgas D. Ace (Fire Fist)", "One Piece"),
            Triple("pet_deku_ofa", "Izuku Midoriya (Gear Shift)", "My Hero Academia"),
            Triple("pet_bakugo_dynamight", "Katsuki Bakugo (Howitzer)", "My Hero Academia"),
            Triple("pet_todoroki_shoto", "Shoto Todoroki (Flashfire)", "My Hero Academia"),
            Triple("pet_all_might_prime", "All Might (United States of Smash)", "My Hero Academia"),
            Triple("pet_makima_control", "Makima (Control Devil)", "Chainsaw Man"),
            Triple("pet_denji_chainsaw", "Denji (Hero of Hell)", "Chainsaw Man"),
            Triple("pet_power_blood", "Power (Blood Fiend)", "Chainsaw Man"),
            Triple("pet_rem_oni", "Rem (Demon Horn)", "Re:Zero"),
            Triple("pet_emilia_ice", "Emilia (Great Spirit)", "Re:Zero"),
            Triple("pet_megumin_explosion", "Megumin (Explosion Magic)", "KonoSuba")
        )
        legendData.forEachIndexed { i, d ->
            pets.add(
                AnimePet(
                    id = d.first,
                    name = d.second,
                    animeOrigin = d.third,
                    rarity = PetRarity.LEGEND,
                    quote = "Waku waku! Siap menemanimu nonton seharian!",
                    powerBonus = "+25% Nobar Room Hype",
                    iconEmoji = "✨",
                    isOwned = i < 4
                )
            )
        }

        // 4. Epic Rarity (20% drop rate)
        val epicData = listOf(
            Triple("pet_mikasa_ackerman", "Mikasa Ackerman", "Attack on Titan"),
            Triple("pet_armin_arlet", "Armin Arlert (Colossal)", "Attack on Titan"),
            Triple("pet_sanji_germa", "Sanji (Ifrit Jambe)", "One Piece"),
            Triple("pet_chopper_monster", "Tony Tony Chopper", "One Piece"),
            Triple("pet_nami_clima", "Nami (Zeus Cloud)", "One Piece"),
            Triple("pet_nicorobin_demon", "Nico Robin (Gigante Fleur)", "One Piece"),
            Triple("pet_yuji_itadori", "Yuji Itadori (Divergent Fist)", "Jujutsu Kaisen"),
            Triple("pet_megumi_fushiguro", "Megumi Fushiguro (Mahoraga)", "Jujutsu Kaisen"),
            Triple("pet_nobara_kugisaki", "Nobara Kugisaki (Resonance)", "Jujutsu Kaisen"),
            Triple("pet_giyu_tomioka", "Giyu Tomioka (Dead Calm)", "Demon Slayer"),
            Triple("pet_shinobu_kocho", "Shinobu Kocho (Insect Hashira)", "Demon Slayer"),
            Triple("pet_muichiro_tokito", "Muichiro Tokito (Mist Hashira)", "Demon Slayer"),
            Triple("pet_tengen_uzui", "Tengen Uzui (Sound Hashira)", "Demon Slayer"),
            Triple("pet_yor_forger", "Yor Forger (Thorn Princess)", "Spy x Family"),
            Triple("pet_loid_forger", "Loid Forger (Agent Twilight)", "Spy x Family"),
            Triple("pet_bond_forger", "Bond (Future Vision Dog)", "Spy x Family"),
            Triple("pet_fern_mage", "Fern (Zoltraak Specialist)", "Frieren"),
            Triple("pet_stark_warrior", "Stark (Dragon Cleaver)", "Frieren"),
            Triple("pet_himmel_hero", "Himmel the Hero", "Frieren"),
            Triple("pet_asta_black", "Asta (Anti Magic Demon)", "Black Clover"),
            Triple("pet_yami_sukehiro", "Yami Sukehiro (Dark Cloaked Blade)", "Black Clover"),
            Triple("pet_kenpachi_zaraki", "Kenpachi Zaraki (Nozarashi)", "Bleach"),
            Triple("pet_byakuya_kuchiki", "Byakuya Kuchiki (Senbonzakura)", "Bleach"),
            Triple("pet_edward_elric", "Edward Elric (Fullmetal)", "Fullmetal Alchemist"),
            Triple("pet_roy_mustang", "Roy Mustang (Flame Alchemist)", "Fullmetal Alchemist")
        )
        epicData.forEachIndexed { i, d ->
            pets.add(
                AnimePet(
                    id = d.first,
                    name = d.second,
                    animeOrigin = d.third,
                    rarity = PetRarity.EPIC,
                    quote = "Kekuatan sejati lahir dari tekad tanpa batas!",
                    powerBonus = "+15% Moppi Chat Shield",
                    iconEmoji = "⚡",
                    isOwned = i < 6
                )
            )
        }

        // 5. Rare Rarity (30% drop rate)
        val rareData = listOf(
            Triple("pet_hisoka_morow", "Hisoka Morow (Bungee Gum)", "Hunter x Hunter"),
            Triple("pet_kurapika_chain", "Kurapika (Emperor Time)", "Hunter x Hunter"),
            Triple("pet_leorio_paladiknight", "Leorio Paradinight", "Hunter x Hunter"),
            Triple("pet_zenitsu_agatsuma", "Zenitsu Agatsuma (Thunderclap)", "Demon Slayer"),
            Triple("pet_inosuke_hashibira", "Inosuke Hashibira (Beast Breathing)", "Demon Slayer"),
            Triple("pet_genya_shinazugawa", "Genya Shinazugawa", "Demon Slayer"),
            Triple("pet_shikamaru_nara", "Shikamaru Nara (Shadow Strangle)", "Naruto"),
            Triple("pet_rock_lee", "Rock Lee (Eight Gates)", "Naruto"),
            Triple("pet_neji_hyuga", "Neji Hyuga (Eight Trigrams)", "Naruto"),
            Triple("pet_gaara_sand", "Gaara of the Sand", "Naruto"),
            Triple("pet_usopp_sogeking", "Usopp (Sogeking)", "One Piece"),
            Triple("pet_franky_super", "Franky (Cyborg Super)", "One Piece"),
            Triple("pet_brook_soul", "Brook (Soul King)", "One Piece"),
            Triple("pet_jinbe_karate", "Jinbe (Fishman Karate)", "One Piece"),
            Triple("pet_bocchi_hitori", "Hitori Gotoh (Bocchi The Rock)", "Bocchi The Rock"),
            Triple("pet_ryo_yamada", "Ryo Yamada", "Bocchi The Rock"),
            Triple("pet_nijika_ijichi", "Nijika Ijichi", "Bocchi The Rock"),
            Triple("pet_kita_ikuyo", "Ikuyo Kita (Kita Aura)", "Bocchi The Rock"),
            Triple("pet_senku_ishigami", "Senku Ishigami (10 Billion Percent)", "Dr. Stone"),
            Triple("pet_gen_asagiri", "Gen Asagiri (Mentalist)", "Dr. Stone")
        )
        rareData.forEachIndexed { i, d ->
            pets.add(
                AnimePet(
                    id = d.first,
                    name = d.second,
                    animeOrigin = d.third,
                    rarity = PetRarity.RARE,
                    quote = "Pantang menyerah sebelum tujuan tercapai!",
                    powerBonus = "+10% Coin Bonus",
                    iconEmoji = "💎",
                    isOwned = i < 8
                )
            )
        }

        // 6. Common Rarity (35% drop rate) - bringing total well over 100 pets
        val commonData = listOf(
            Triple("pet_kon_bleach", "Kon (Stuffed Lion)", "Bleach"),
            Triple("pet_happy_fairy", "Happy (Aera Cat)", "Fairy Tail"),
            Triple("pet_puck_spirit", "Puck (Spirit Cat)", "Re:Zero"),
            Triple("pet_chomusuke_cat", "Chomusuke (Winged Cat)", "KonoSuba"),
            Triple("pet_akakabuto_bear", "Akamaru (Ninja Dog)", "Naruto"),
            Triple("pet_pakun_pug", "Pakkun (Ninken)", "Naruto"),
            Triple("pet_gamakichi_toad", "Gamakichi (Summon Toad)", "Naruto"),
            Triple("pet_karoo_duck", "Karoo (Super Spot-Billed Duck)", "One Piece"),
            Triple("pet_den_den_mushi", "Den Den Mushi (Transponder Snail)", "One Piece"),
            Triple("pet_laboon_whale", "Laboon Baby", "One Piece"),
            Triple("pet_monokuma_bear", "Monokuma", "Danganronpa"),
            Triple("pet_pochita_chainsaw", "Pochita (Chainsaw Pup)", "Chainsaw Man"),
            Triple("pet_meowth_rocket", "Meowth (Pay Day)", "Pokemon"),
            Triple("pet_pikachu_mascot", "Pikachu (Volt Mascot)", "Pokemon"),
            Triple("pet_totoro_spirit", "Mini Totoro", "Studio Ghibli"),
            Triple("pet_kiki_jiji", "Jiji the Black Cat", "Kiki Delivery"),
            Triple("pet_calcifer_fire", "Calcifer Fire Demon", "Howl Moving Castle"),
            Triple("pet_soot_sprite", "Soot Sprite (Susuwatari)", "Spirited Away"),
            Triple("pet_tony_reindeer", "Baby Chopper Reindeer", "One Piece"),
            Triple("pet_ein_corgi", "Ein (Data Dog Corgi)", "Cowboy Bebop")
        )
        commonData.forEachIndexed { i, d ->
            pets.add(
                AnimePet(
                    id = d.first,
                    name = d.second,
                    animeOrigin = d.third,
                    rarity = PetRarity.COMMON,
                    quote = "Selalu setia menemani waktu nonton santaimu!",
                    powerBonus = "+5% Gem Gain",
                    iconEmoji = "🐾",
                    isOwned = true
                )
            )
        }

        return pets
    }

    // 50 Languages support as requested
    private fun create50Languages(): List<SupportedLanguage> {
        return listOf(
            SupportedLanguage("id", "Indonesia", "Bahasa Indonesia", "🇮🇩"),
            SupportedLanguage("en", "English", "English", "🇺🇸"),
            SupportedLanguage("ja", "Japanese", "日本語", "🇯🇵"),
            SupportedLanguage("ko", "Korean", "한국어", "🇰🇷"),
            SupportedLanguage("zh", "Chinese (Simplified)", "简体中文", "🇨🇳"),
            SupportedLanguage("zh-TW", "Chinese (Traditional)", "繁體中文", "🇹🇼"),
            SupportedLanguage("es", "Spanish", "Español", "🇪🇸"),
            SupportedLanguage("fr", "French", "Français", "🇫🇷"),
            SupportedLanguage("de", "German", "Deutsch", "🇩🇪"),
            SupportedLanguage("pt", "Portuguese", "Português", "🇧🇷"),
            SupportedLanguage("ru", "Russian", "Русский", "🇷🇺"),
            SupportedLanguage("ar", "Arabic", "العربية", "🇸🇦"),
            SupportedLanguage("hi", "Hindi", "हिन्दी", "🇮🇳"),
            SupportedLanguage("th", "Thai", "ไทย", "🇹🇭"),
            SupportedLanguage("vi", "Vietnamese", "Tiếng Việt", "🇻🇳"),
            SupportedLanguage("it", "Italian", "Italiano", "🇮🇹"),
            SupportedLanguage("tr", "Turkish", "Türkçe", "🇹🇷"),
            SupportedLanguage("nl", "Dutch", "Nederlands", "🇳🇱"),
            SupportedLanguage("pl", "Polish", "Polski", "🇵🇱"),
            SupportedLanguage("sv", "Swedish", "Svenska", "🇸🇪"),
            SupportedLanguage("tl", "Tagalog", "Filipino", "🇵🇭"),
            SupportedLanguage("ms", "Malay", "Bahasa Melayu", "🇲🇾"),
            SupportedLanguage("el", "Greek", "Ελληνικά", "🇬🇷"),
            SupportedLanguage("cs", "Czech", "Čeština", "🇨🇿"),
            SupportedLanguage("da", "Danish", "Dansk", "🇩🇰"),
            SupportedLanguage("fi", "Finnish", "Suomi", "🇫🇮"),
            SupportedLanguage("hu", "Hungarian", "Magyar", "🇭🇺"),
            SupportedLanguage("no", "Norwegian", "Norsk", "🇳🇴"),
            SupportedLanguage("ro", "Romanian", "Română", "🇷🇴"),
            SupportedLanguage("uk", "Ukrainian", "Українська", "🇺🇦"),
            SupportedLanguage("he", "Hebrew", "עברית", "🇮🇱"),
            SupportedLanguage("bn", "Bengali", "বাংলা", "🇧🇩"),
            SupportedLanguage("fa", "Persian", "فارسی", "🇮🇷"),
            SupportedLanguage("ur", "Urdu", "اردو", "🇵🇰"),
            SupportedLanguage("sw", "Swahili", "Kiswahili", "🇰🇪"),
            SupportedLanguage("jv", "Javanese", "Basa Jawa", "🇮🇩"),
            SupportedLanguage("su", "Sundanese", "Basa Sunda", "🇮🇩"),
            SupportedLanguage("my", "Burmese", "မြန်မာစာ", "🇲🇲"),
            SupportedLanguage("km", "Khmer", "ភាសាខ្មែរ", "🇰🇭"),
            SupportedLanguage("lo", "Lao", "ພາສາລາວ", "🇱🇦"),
            SupportedLanguage("ta", "Tamil", "தமிழ்", "🇮🇳"),
            SupportedLanguage("te", "Telugu", "తెలుగు", "🇮🇳"),
            SupportedLanguage("mr", "Marathi", "मराठी", "🇮🇳"),
            SupportedLanguage("pa", "Punjabi", "ਪੰਜਾਬੀ", "🇮🇳"),
            SupportedLanguage("gu", "Gujarati", "ગુજરાતી", "🇮🇳"),
            SupportedLanguage("sk", "Slovak", "Slovenčina", "🇸🇰"),
            SupportedLanguage("bg", "Bulgarian", "Български", "🇧🇬"),
            SupportedLanguage("hr", "Croatian", "Hrvatski", "🇭🇷"),
            SupportedLanguage("sr", "Serbian", "Српски", "🇷🇸"),
            SupportedLanguage("af", "Afrikaans", "Afrikaans", "🇿🇦")
        )
    }

    private fun createDefaultSchedule(): List<StenlyScheduleDay> {
        return listOf(
            StenlyScheduleDay("Senin", listOf(StenlyScheduleAnime("Solo Leveling: Arise", slug = "solo-leveling"))),
            StenlyScheduleDay("Selasa", listOf(StenlyScheduleAnime("Demon Slayer: Infinity Castle", slug = "demon-slayer"))),
            StenlyScheduleDay("Rabu", listOf(StenlyScheduleAnime("The Eminence in Shadow S3", slug = "eminence-shadow"))),
            StenlyScheduleDay("Kamis", listOf(StenlyScheduleAnime("Jujutsu Kaisen: Culling Game", slug = "jujutsu-kaisen"))),
            StenlyScheduleDay("Jumat", listOf(StenlyScheduleAnime("Frieren: Beyond Journey's End", slug = "frieren"))),
            StenlyScheduleDay("Sabtu", listOf(StenlyScheduleAnime("Bleach: Thousand-Year Blood War", slug = "bleach"))),
            StenlyScheduleDay("Minggu", listOf(StenlyScheduleAnime("One Piece: Egghead Arc", slug = "one-piece")))
        )
    }

    private fun createDefaultGenres(): List<StenlyGenreItem> {
        return listOf(
            StenlyGenreItem("Action", "action"),
            StenlyGenreItem("Adventure", "adventure"),
            StenlyGenreItem("Comedy", "comedy"),
            StenlyGenreItem("Drama", "drama"),
            StenlyGenreItem("Fantasy", "fantasy"),
            StenlyGenreItem("Isekai", "isekai"),
            StenlyGenreItem("Romance", "romance"),
            StenlyGenreItem("Sci-Fi", "sci-fi"),
            StenlyGenreItem("Shounen", "shounen"),
            StenlyGenreItem("Supernatural", "supernatural")
        )
    }
}
