package com.example.data.model

data class Episode(
    val episodeNumber: Int,
    val title: String,
    val duration: String = "24:12",
    val introStartSec: Int = 90,
    val introEndSec: Int = 180,
    val thumbnailUrl: String? = null,
    val thumbnailGradient: List<Long> = listOf(0xFF2563EB, 0xFF06B6D4),
    val videoUrl: String = "https://www.youtube.com/watch?v=91t0qWfLgQc",
    val rawVideoUrl: String? = null,
    val slug: String? = null
)

data class Anime(
    val id: String,
    val title: String,
    val japaneseTitle: String,
    val synopsis: String,
    val genres: List<String>,
    val rating: Double,
    val rank: Int,
    val season: String,
    val studio: String,
    val totalEpisodes: Int,
    val status: String, // Ongoing, Finished
    val isSimulcast: Boolean = false,
    val nextEpisodeCountdownHours: Int = 14,
    val posterUrl: String? = null,
    val trailerUrl: String? = null,
    val bannerDrawableRes: Int? = null,
    val episodes: List<Episode> = emptyList(),
    val isPremiumOnly: Boolean = false,
    val slug: String? = null,
    val batchLink: String? = null,
    val dayUpdated: String? = null
)

enum class VideoPlayerMode {
    NORMAL,
    VR_CINEMA_360
}

data class SupportedLanguage(
    val code: String,
    val name: String,
    val nativeName: String,
    val flag: String
)
