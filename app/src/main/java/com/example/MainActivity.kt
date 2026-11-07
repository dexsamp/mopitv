package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MoppiTab
import com.example.ui.MoppiViewModel
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.MoppiSurfaceSubtle
import com.example.ui.theme.MoppiWhite
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MoppiAppRoot()
            }
        }
    }
}

@Composable
fun MoppiAppRoot(
    viewModel: MoppiViewModel = viewModel()
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val announcements by viewModel.announcements.collectAsStateWithLifecycle()
    val animeList by viewModel.animeList.collectAsStateWithLifecycle()
    val nobarRooms by viewModel.nobarRooms.collectAsStateWithLifecycle()
    val animeShorts by viewModel.animeShorts.collectAsStateWithLifecycle()
    val petsList by viewModel.petsList.collectAsStateWithLifecycle()
    val framesList by viewModel.framesList.collectAsStateWithLifecycle()
    val publicChat by viewModel.publicChat.collectAsStateWithLifecycle()
    val guilds by viewModel.guilds.collectAsStateWithLifecycle()
    val achievements by viewModel.achievements.collectAsStateWithLifecycle()
    val supportedLanguages = viewModel.supportedLanguages
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()

    val selectedAnime by viewModel.selectedAnime.collectAsStateWithLifecycle()
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()
    val activeNobarRoom by viewModel.activeNobarRoom.collectAsStateWithLifecycle()
    val isVoiceMicOn by viewModel.isVoiceMicOn.collectAsStateWithLifecycle()
    val isVoiceDeafened by viewModel.isVoiceDeafened.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedGenre by viewModel.selectedGenre.collectAsStateWithLifecycle()

    val scheduleDays by viewModel.scheduleDays.collectAsStateWithLifecycle()
    val selectedScheduleDay by viewModel.selectedScheduleDay.collectAsStateWithLifecycle()
    val otakuGenres by viewModel.otakuGenres.collectAsStateWithLifecycle()

    val showAdminPanel by viewModel.showAdminPanel.collectAsStateWithLifecycle()
    val showGiftDialog by viewModel.showGiftDialog.collectAsStateWithLifecycle()
    val showLanguageDialog by viewModel.showLanguageDialog.collectAsStateWithLifecycle()
    val showCreateRoomDialog by viewModel.showCreateRoomDialog.collectAsStateWithLifecycle()

    val gachaResult by viewModel.gachaResult.collectAsStateWithLifecycle()
    val isSpinning by viewModel.isSpinning.collectAsStateWithLifecycle()
    val petSpeechBubble by viewModel.petSpeechBubble.collectAsStateWithLifecycle()

    val equippedFrame = remember(framesList, userProfile.equippedFrameId) {
        framesList.find { it.id == userProfile.equippedFrameId }
    }
    val equippedPet = remember(petsList, userProfile.equippedPetId) {
        petsList.find { it.id == userProfile.equippedPetId }
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.initFirestore(context)
    }

    // Back handling
    BackHandler(enabled = playerState.isPlayerActive || selectedAnime != null || currentTab != MoppiTab.HOME) {
        when {
            playerState.isPlayerActive -> viewModel.closePlayer()
            selectedAnime != null -> viewModel.closeAnimeDetail()
            currentTab != MoppiTab.HOME -> viewModel.selectTab(MoppiTab.HOME)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MoppiSurfaceSubtle),
        topBar = {
            if (!playerState.isPlayerActive && currentTab != MoppiTab.SHORTS) {
                MoppiTopBar(
                    user = userProfile,
                    equippedFrame = equippedFrame,
                    selectedLang = selectedLanguage,
                    onOpenGems = { viewModel.selectTab(MoppiTab.PETS) },
                    onOpenLanguages = { viewModel.showLanguageDialog.value = true },
                    onOpenAdmin = { viewModel.showAdminPanel.value = true },
                    onOpenProfile = { viewModel.selectTab(MoppiTab.PROFILE) }
                )
            }
        },
        bottomBar = {
            if (!playerState.isPlayerActive) {
                MoppiBottomBar(
                    selectedTab = currentTab,
                    onTabSelected = {
                        viewModel.closeAnimeDetail()
                        viewModel.selectTab(it)
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Tab View
            when (currentTab) {
                MoppiTab.HOME -> {
                    HomeScreen(
                        animeList = animeList,
                        announcements = announcements,
                        searchQuery = searchQuery,
                        onSearchChange = {
                            viewModel.searchQuery.value = it
                            viewModel.searchOtakudesu(it)
                        },
                        selectedGenre = selectedGenre,
                        onGenreSelect = { g ->
                            val genreObj = otakuGenres.find { it.name.equals(g, ignoreCase = true) }
                            if (genreObj != null) {
                                viewModel.filterByGenre(genreObj.cleanSlug)
                            }
                            viewModel.selectedGenre.value = g
                        },
                        scheduleDays = scheduleDays,
                        selectedScheduleDay = selectedScheduleDay,
                        onSelectScheduleDay = { viewModel.selectScheduleDay(it) },
                        genresList = otakuGenres,
                        onAnimeClick = { viewModel.openAnimeDetail(it) },
                        onDirectPlay = { anime, mode ->
                            val firstEp = anime.episodes.firstOrNull() ?: com.example.data.model.Episode(1, "Eps 1")
                            viewModel.startPlayingAnime(anime, firstEp, mode)
                        }
                    )
                }

                MoppiTab.NOBAR -> {
                    val activeRoomMessages by remember(activeNobarRoom?.id) {
                        if (activeNobarRoom != null) {
                            viewModel.observeRoomMessages(activeNobarRoom!!.id)
                        } else {
                            kotlinx.coroutines.flow.flowOf(emptyList())
                        }
                    }.collectAsStateWithLifecycle(emptyList())

                    WatchParty(
                        nobarRooms = nobarRooms,
                        activeRoom = activeNobarRoom,
                        userProfile = userProfile,
                        roomMessages = activeRoomMessages,
                        onSendChatMessage = { roomId, msg -> viewModel.sendNobarRoomMessage(roomId, msg) },
                        isVoiceMicOn = isVoiceMicOn,
                        isVoiceDeafened = isVoiceDeafened,
                        onToggleMic = { viewModel.toggleVoiceMic() },
                        onToggleDeafen = { viewModel.toggleVoiceDeafen() },
                        onJoinRoom = { viewModel.joinNobarRoom(it) },
                        onLeaveRoom = { viewModel.leaveNobarRoom() },
                        onCreateRoomClick = { viewModel.showCreateRoomDialog.value = true }
                    )
                }

                MoppiTab.SHORTS -> {
                    AnimeReels(
                        shorts = animeShorts,
                        onToggleLike = { viewModel.toggleShortLike(it) },
                        onWatchFullAnime = { animeTitle ->
                            val match = animeList.find { it.title.contains(animeTitle, ignoreCase = true) } ?: animeList.first()
                            viewModel.startPlayingAnime(match, match.episodes.first())
                        },
                        onRefreshClips = { viewModel.refreshAnimeCatalog() }
                    )
                }

                MoppiTab.PETS -> {
                    GachaPet(
                        pets = petsList,
                        userGems = userProfile.moppiGems,
                        isSpinning = isSpinning,
                        gachaResults = gachaResult,
                        equippedPetId = userProfile.equippedPetId,
                        onSpin = { viewModel.spinGacha(it) },
                        onDismissResult = { viewModel.dismissGachaResult() },
                        onEquipPet = { viewModel.equipPet(it) },
                        onAddGemsClick = { viewModel.showAdminPanel.value = true }
                    )
                }

                MoppiTab.COMMUNITY -> {
                    CommunityScreen(
                        user = userProfile,
                        guilds = guilds,
                        chatMessages = publicChat,
                        onSendMessage = { viewModel.sendPublicMessage(it) },
                        onJoinGuild = { viewModel.joinGuild(it) },
                        onOpenGiftDialog = { viewModel.showGiftDialog.value = true }
                    )
                }

                MoppiTab.PROFILE -> {
                    ProfileScreen(
                        user = userProfile,
                        equippedFrame = equippedFrame,
                        equippedPet = equippedPet,
                        achievements = achievements,
                        onClaimAchievement = { viewModel.claimAchievement(it) },
                        onSwitchRole = { viewModel.switchRole(it) },
                        onTogglePremium = { viewModel.togglePremium(it) },
                        onChangeBackground = { viewModel.updateProfileBackground(it) },
                        onOpenAdminPanel = { viewModel.showAdminPanel.value = true },
                        onOpenLanguages = { viewModel.showLanguageDialog.value = true },
                        onOpenGiftDialog = { viewModel.showGiftDialog.value = true }
                    )
                }
            }

            // 2. Floating Anime Pet Mascot (Visible on Home, Nobar, Community)
            if (currentTab != MoppiTab.SHORTS && !playerState.isPlayerActive) {
                MoppiFloatingPet(
                    equippedPet = equippedPet,
                    speechQuote = petSpeechBubble,
                    onPetClicked = { viewModel.triggerPetQuote() },
                    onDismissQuote = { viewModel.petSpeechBubble.value = null },
                    modifier = Modifier.align(Alignment.BottomEnd)
                )
            }

            // Detail Modal Overlay (Only active while in HOME catalog tab)
            if (currentTab == MoppiTab.HOME) {
                selectedAnime?.let { anime ->
                    AnimeDetailModal(
                        anime = anime,
                        onDismiss = { viewModel.closeAnimeDetail() },
                        onPlayEpisode = { targetAnime, ep, mode ->
                            viewModel.startPlayingAnime(targetAnime, ep, mode)
                        }
                    )
                }
            }

            // Video Player Sheet Overlay
            if (playerState.isPlayerActive && playerState.anime != null) {
                MoppiPlayerSheet(
                    playerState = playerState,
                    onClose = { viewModel.closePlayer() },
                    onTogglePlay = { viewModel.togglePlayPause() },
                    onSkipIntro = { viewModel.skipIntro() },
                    onToggleVrMode = { viewModel.togglePlayerMode() },
                    onSeek = { viewModel.seekTo(it) },
                    onSelectEpisode = { ep ->
                        playerState.anime?.let { a -> viewModel.startPlayingAnime(a, ep, playerState.mode) }
                    },
                    onOpenSubtitles = { viewModel.showLanguageDialog.value = true },
                    onToggleTrailerMode = { viewModel.toggleTrailerMode() }
                )
            }
        }
    }

    // Modal Dialogs
    if (showAdminPanel) {
        AdminPanelDialog(
            onDismiss = { viewModel.showAdminPanel.value = false },
            onBroadcast = { t, m, u -> viewModel.publishAnnouncement(t, m, u) },
            onIssueWarn = { u, r -> viewModel.issueWarn(u, r) },
            onAddFreeGems = { viewModel.addGems(it) }
        )
    }

    if (showGiftDialog) {
        GiftDialog(
            userGems = userProfile.moppiGems,
            onDismiss = { viewModel.showGiftDialog.value = false },
            onSendGift = { u, g, p -> viewModel.giftGemOrPremium(u, g, p) }
        )
    }

    if (showLanguageDialog) {
        LanguageSelectorDialog(
            languages = supportedLanguages,
            currentLang = selectedLanguage,
            onSelectLanguage = { viewModel.setAppLanguage(it) },
            onDismiss = { viewModel.showLanguageDialog.value = false }
        )
    }

    if (showCreateRoomDialog) {
        CreateRoomDialog(
            onDismiss = { viewModel.showCreateRoomDialog.value = false },
            onCreate = { title, anime, isPriv, pin ->
                viewModel.createNobarRoom(title, anime, isPriv, pin)
                viewModel.showCreateRoomDialog.value = false
            }
        )
    }
}
