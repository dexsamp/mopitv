package com.example.data.model

data class NobarRoom(
    val id: String,
    val title: String,
    val animeTitle: String,
    val episodeNumber: Int,
    val hostUsername: String,
    val hostRole: UserRole,
    val isPrivate: Boolean,
    val pinCode: String? = null,
    val participantCount: Int,
    val maxParticipants: Int = 50,
    val isVoiceEnabled: Boolean = true,
    val currentTimestampSeconds: Int = 340,
    val isLivePlaying: Boolean = true,
    val videoUrl: String = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
    val thumbnailUrl: String? = null
)

data class NobarChatMessage(
    val id: String = "",
    val senderName: String = "",
    val senderRole: UserRole = UserRole.WARGA,
    val isSenderPremium: Boolean = false,
    val senderUid: String = "",
    val senderAvatarEmoji: String = "🍿",
    val message: String = "",
    val timestamp: String = "12:00"
)

data class AnimeShort(
    val id: String,
    val title: String,
    val animeSource: String,
    val uploaderName: String,
    val uploaderRole: UserRole,
    val likesCount: Int,
    val commentsCount: Int,
    val sharesCount: Int,
    val isLiked: Boolean = false,
    val videoStreamUrl: String = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
    val thumbnailUrl: String? = null,
    val gradientColors: List<Long> = listOf(0xFF1E293B, 0xFF0F172A),
    val durationText: String = "0:45",
    val tags: List<String> = listOf("#anime", "#moppitv", "#epicbattle")
)

data class GuildSquad(
    val id: String,
    val name: String,
    val tag: String,
    val leaderName: String,
    val memberCount: Int,
    val maxMembers: Int = 30,
    val rankPosition: Int,
    val trophyScore: Int,
    val slogan: String,
    val isJoined: Boolean = false
)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val currentProgress: Int,
    val targetProgress: Int,
    val gemReward: Int,
    val isClaimed: Boolean = false
)
