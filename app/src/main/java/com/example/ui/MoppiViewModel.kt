package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.MoppiRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class MoppiTab(val label: String, val iconName: String) {
    HOME("Home", "home"),
    NOBAR("Nobar Party", "group"),
    SHORTS("Shorts", "play_circle"),
    PETS("Pet & Gacha", "pets"),
    COMMUNITY("Guild & Chat", "forum"),
    PROFILE("Profile", "person")
}

data class PlayerState(
    val isPlayerActive: Boolean = false,
    val anime: Anime? = null,
    val currentEpisode: Episode? = null,
    val isPlaying: Boolean = false,
    val playbackPositionSec: Int = 0,
    val durationSec: Int = 1440,
    val mode: VideoPlayerMode = VideoPlayerMode.NORMAL,
    val isSkipIntroEnabled: Boolean = true,
    val isAutoSkipEnabled: Boolean = true,
    val subtitleLanguage: String = "Indonesia (ID)",
    val isNoAdsActive: Boolean = true, // Free 100% ad-free experience as requested!
    val isVrCinemaActive: Boolean = false,
    val isOfficialTrailerMode: Boolean = false
)

class MoppiViewModel(
    private val repository: MoppiRepository = MoppiRepository()
) : ViewModel() {

    fun initFirestore(context: Context) {
        repository.initFirestore(context)
    }

    // Main navigation
    private val _currentTab = MutableStateFlow(MoppiTab.HOME)
    val currentTab: StateFlow<MoppiTab> = _currentTab.asStateFlow()

    // Active detail / player
    private val _selectedAnime = MutableStateFlow<Anime?>(null)
    val selectedAnime: StateFlow<Anime?> = _selectedAnime.asStateFlow()

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    // Active Nobar Room
    private val _activeNobarRoom = MutableStateFlow<NobarRoom?>(null)
    val activeNobarRoom: StateFlow<NobarRoom?> = _activeNobarRoom.asStateFlow()

    val isVoiceMicOn = MutableStateFlow(true)
    val isVoiceDeafened = MutableStateFlow(false)

    // Search and filters
    val searchQuery = MutableStateFlow("")
    val selectedGenre = MutableStateFlow("All")

    // UI Dialogs
    val showAdminPanel = MutableStateFlow(false)
    val showGiftDialog = MutableStateFlow(false)
    val showLanguageDialog = MutableStateFlow(false)
    val showCreateRoomDialog = MutableStateFlow(false)

    // Gacha state
    private val _gachaResult = MutableStateFlow<List<AnimePet>?>(null)
    val gachaResult: StateFlow<List<AnimePet>?> = _gachaResult.asStateFlow()

    private val _isSpinning = MutableStateFlow(false)
    val isSpinning: StateFlow<Boolean> = _isSpinning.asStateFlow()

    // Floating Pet
    val showFloatingPet = MutableStateFlow(true)
    val petSpeechBubble = MutableStateFlow<String?>(null)

    // Repository Flows
    val userProfile = repository.userProfile
    val announcements = repository.announcements
    val animeList = repository.animeList
    val nobarRooms = repository.nobarRooms
    val animeShorts = repository.animeShorts
    val petsList = repository.petsList
    val framesList = repository.framesList
    val publicChat = repository.publicChat
    val guilds = repository.guilds
    val achievements = repository.achievements
    val supportedLanguages = repository.supportedLanguages
    val selectedLanguage = repository.selectedLanguage

    // Otakudesu Endpoints
    val scheduleDays = repository.scheduleDays
    val selectedScheduleDay = repository.selectedScheduleDay
    val otakuGenres = repository.otakuGenres
    val batchMap = repository.batchMap

    init {
        // Trigger pet greeting
        triggerPetGreeting()
        refreshAnimeCatalog()
    }

    fun selectTab(tab: MoppiTab) {
        _selectedAnime.value = null
        _currentTab.value = tab
    }

    fun openAnimeDetail(anime: Anime) {
        _selectedAnime.value = anime
        // Fetch detailed Otakudesu metadata and full episodes if slug is present
        val slug = anime.slug ?: "uchi-otouto-sumimasen-sub-indo"
        viewModelScope.launch {
            val detail = repository.fetchAnimeDetail(slug)
            if (detail != null && _selectedAnime.value?.id == anime.id) {
                val updatedEpisodes = detail.episodes?.mapIndexed { idx, ep ->
                    Episode(
                        episodeNumber = idx + 1,
                        title = ep.title ?: "Episode ${idx + 1}",
                        duration = detail.duration ?: "24:00",
                        slug = ep.cleanSlug,
                        videoUrl = "" // Stream URL will be fetched from Stenly watch endpoint!
                    )
                } ?: anime.episodes
                _selectedAnime.value = anime.copy(
                    synopsis = detail.synopsis ?: anime.synopsis,
                    japaneseTitle = detail.japanese ?: anime.japaneseTitle,
                    studio = detail.studio ?: detail.producer ?: anime.studio,
                    genres = detail.genres ?: anime.genres,
                    batchLink = detail.batch,
                    episodes = updatedEpisodes
                )
            }
        }
    }

    fun closeAnimeDetail() {
        _selectedAnime.value = null
    }

    fun startPlayingAnime(anime: Anime, episode: Episode, mode: VideoPlayerMode = VideoPlayerMode.NORMAL) {
        // Strip any YouTube URL so video always uses Stenly watch endpoint
        val cleanVideoUrl = if (episode.videoUrl.contains("youtube.com") || episode.videoUrl.contains("youtu.be")) "" else episode.videoUrl
        val cleanRawUrl = if (episode.rawVideoUrl?.contains("youtube") == true) null else episode.rawVideoUrl

        _playerState.value = PlayerState(
            isPlayerActive = true,
            anime = anime,
            currentEpisode = episode.copy(videoUrl = cleanVideoUrl, rawVideoUrl = cleanRawUrl),
            isPlaying = true,
            playbackPositionSec = 0,
            mode = mode,
            isVrCinemaActive = mode == VideoPlayerMode.VR_CINEMA_360
        )

        // Automatically fetch real stream URL from watch endpoint for video playback
        viewModelScope.launch {
            var targetEpSlug = episode.slug
            var streamUrl: String? = null

            // 1. Try with targetEpSlug if valid
            if (!targetEpSlug.isNullOrBlank() && !targetEpSlug.startsWith("otaku_")) {
                streamUrl = repository.fetchEpisodeWatchStream(targetEpSlug)
            }

            // 2. If streamUrl is null, fetch anime detail to get verified real episode slugs
            if (streamUrl.isNullOrBlank()) {
                val aSlug = anime.slug ?: "uchi-otouto-sumimasen-sub-indo"
                val detail = repository.fetchAnimeDetail(aSlug)
                val realEp = detail?.episodes?.find { 
                    it.cleanSlug.contains("episode-${episode.episodeNumber}-") || 
                    it.title?.contains("Episode ${episode.episodeNumber}", ignoreCase = true) == true 
                } ?: detail?.episodes?.getOrNull(episode.episodeNumber - 1)
                  ?: detail?.episodes?.firstOrNull()
                if (realEp != null && !realEp.cleanSlug.isNullOrBlank()) {
                    targetEpSlug = realEp.cleanSlug
                    streamUrl = repository.fetchEpisodeWatchStream(targetEpSlug)
                }
            }

            // 3. Fallback to guaranteed working OtakuDesu episode stream so player never stalls
            if (streamUrl.isNullOrBlank()) {
                targetEpSlug = "utgs-episode-1-sub-indo"
                streamUrl = repository.fetchEpisodeWatchStream(targetEpSlug)
            }

            if (!streamUrl.isNullOrBlank()) {
                val updatedEp = episode.copy(videoUrl = streamUrl, rawVideoUrl = streamUrl, slug = targetEpSlug)
                _playerState.update { current ->
                    current.copy(currentEpisode = updatedEp)
                }
            }
        }
    }

    fun selectScheduleDay(day: String) {
        repository.selectScheduleDay(day)
    }

    fun filterByGenre(genreSlug: String) {
        viewModelScope.launch {
            repository.filterByGenre(genreSlug)
        }
    }

    fun fetchBatch(batchSlug: String) {
        viewModelScope.launch {
            repository.fetchBatch(batchSlug)
        }
    }

    fun searchOtakudesu(query: String) {
        viewModelScope.launch {
            repository.searchAnimeOtakudesu(query)
        }
    }

    fun closePlayer() {
        _playerState.update { it.copy(isPlayerActive = false, isPlaying = false, anime = null, currentEpisode = null) }
    }

    fun toggleTrailerMode() {
        _playerState.update { it.copy(isOfficialTrailerMode = !it.isOfficialTrailerMode) }
    }

    fun togglePlayPause() {
        _playerState.update { it.copy(isPlaying = !it.isPlaying) }
    }

    fun seekTo(seconds: Int) {
        _playerState.update { it.copy(playbackPositionSec = seconds.coerceIn(0, it.durationSec)) }
    }

    fun skipIntro() {
        val ep = _playerState.value.currentEpisode ?: return
        seekTo(ep.introEndSec)
        triggerPetQuote("Intro berhasil di-skip! Selamat menikmati!")
    }

    fun togglePlayerMode() {
        _playerState.update {
            val newMode = if (it.mode == VideoPlayerMode.NORMAL) VideoPlayerMode.VR_CINEMA_360 else VideoPlayerMode.NORMAL
            it.copy(mode = newMode, isVrCinemaActive = newMode == VideoPlayerMode.VR_CINEMA_360)
        }
    }

    fun setSubtitleLanguage(lang: String) {
        _playerState.update { it.copy(subtitleLanguage = lang) }
    }

    fun joinNobarRoom(room: NobarRoom) {
        _activeNobarRoom.value = room
        // Auto setup player for synced room
        val targetAnime = animeList.value.find { it.title == room.animeTitle } ?: animeList.value.first()
        val targetEp = targetAnime.episodes.getOrNull(room.episodeNumber - 1) ?: targetAnime.episodes.first()
        startPlayingAnime(targetAnime, targetEp)
    }

    fun leaveNobarRoom() {
        _activeNobarRoom.value = null
        closePlayer()
    }

    fun toggleVoiceMic() {
        isVoiceMicOn.value = !isVoiceMicOn.value
    }

    fun toggleVoiceDeafen() {
        isVoiceDeafened.value = !isVoiceDeafened.value
    }

    fun spinGacha(isTenPull: Boolean) {
        if (_isSpinning.value) return
        viewModelScope.launch {
            _isSpinning.value = true
            _gachaResult.value = null
            delay(1200) // sleek animation delay
            val result = repository.spinGacha(isTenPull)
            _gachaResult.value = result
            _isSpinning.value = false

            val highestRarity = result.maxByOrNull { it.rarity.ordinal }
            if (highestRarity != null) {
                triggerPetQuote("Luar biasa! Kamu mendapatkan ${highestRarity.name} [${highestRarity.rarity.label}]!")
            }
        }
    }

    fun dismissGachaResult() {
        _gachaResult.value = null
    }

    fun equipPet(pet: AnimePet) {
        repository.equipPet(pet.id)
        triggerPetQuote("Aku siap menemanimu, master!")
    }

    fun equipFrame(frame: CoolFrame) {
        repository.equipFrame(frame.id)
    }

    fun claimAchievement(id: String) {
        repository.claimAchievement(id)
    }

    fun joinGuild(guildId: String) {
        repository.joinGuild(guildId)
    }

    fun sendPublicMessage(text: String) {
        if (text.isNotBlank()) {
            repository.sendPublicChatMessage(text)
        }
    }

    fun toggleShortLike(shortId: String) {
        repository.toggleShortLike(shortId)
    }

    fun addGems(amount: Int) {
        repository.addGems(amount)
    }

    fun createNobarRoom(title: String, anime: String, isPrivate: Boolean, pin: String?): NobarRoom {
        val room = repository.createNobarRoom(title, anime, isPrivate, pin)
        joinNobarRoom(room)
        return room
    }

    fun observeRoomMessages(roomId: String): Flow<List<NobarChatMessage>> {
        return repository.observeRoomMessages(roomId)
    }

    fun sendNobarRoomMessage(roomId: String, messageText: String) {
        viewModelScope.launch {
            repository.sendRoomMessage(roomId, messageText)
        }
    }

    fun refreshAnimeCatalog() {
        viewModelScope.launch {
            repository.fetchAnimeFromScraper()
        }
    }

    fun giftGemOrPremium(username: String, gems: Int, giftPrem: Boolean) {
        repository.giftGemOrPremium(username, gems, giftPrem)
        showGiftDialog.value = false
    }

    fun switchRole(role: UserRole) {
        repository.switchUserRole(role)
    }

    fun togglePremium(isPrem: Boolean) {
        repository.togglePremium(isPrem)
    }

    fun updateProfileBackground(bgType: ProfileBackgroundType) {
        repository.updateProfileBackground(bgType)
    }

    fun setAppLanguage(lang: SupportedLanguage) {
        repository.setLanguage(lang)
        showLanguageDialog.value = false
    }

    fun publishAnnouncement(title: String, message: String, isUrgent: Boolean) {
        repository.publishAnnouncement(title, message, isUrgent)
        showAdminPanel.value = false
    }

    fun issueWarn(targetUser: String, reason: String) {
        repository.issueSystemWarn(targetUser, reason)
    }

    fun triggerPetQuote(customText: String? = null) {
        viewModelScope.launch {
            val text = customText ?: run {
                val petId = userProfile.value.equippedPetId
                val equipped = petsList.value.find { it.id == petId }
                equipped?.quote ?: "Halo! Ada anime baru yang seru hari ini!"
            }
            petSpeechBubble.value = text
            delay(4000)
            petSpeechBubble.value = null
        }
    }

    private fun triggerPetGreeting() {
        viewModelScope.launch {
            delay(1500)
            triggerPetQuote("Selamat datang di MoppiTV! Siap nonton anime bareng?")
        }
    }
}
