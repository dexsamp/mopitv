package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class UserRole(
    val title: String,
    val badgeColor: Color,
    val hasRedChecklist: Boolean = false,
    val hasBlueChecklist: Boolean = false,
    val hasPurpleChecklist: Boolean = false
) {
    DEVELOPER("Developer", BadgeDeveloperRed, hasRedChecklist = true),
    ADMIN("Admin/Moderator", BadgeAdminPurple, hasPurpleChecklist = true),
    SEPUH("Sepuh", BadgeSepuhAmber),
    WARGA("Warga", BadgeWargaSlate)
}

data class UserProfile(
    val id: String = "user_777",
    val username: String = "MoppiMaster",
    val role: UserRole = UserRole.DEVELOPER, // User can switch role to preview Developer, Admin, Sepuh, Warga
    val isPremium: Boolean = true, // gives blue checklist!
    val moppiGems: Int = 1250,
    val equippedFrameId: String = "frame_galaxy_nebula",
    val equippedPetId: String = "pet_satoru_gojo",
    val backgroundGifType: ProfileBackgroundType = ProfileBackgroundType.CYBER_NEON_GRID,
    val warningsCount: Int = 0,
    val warningReason: String? = null,
    val guildId: String? = "guild_akatsuki",
    val guildName: String? = "Akatsuki Cyber",
    val episodesWatched: Int = 142,
    val gachaSpinsCount: Int = 38
)

enum class ProfileBackgroundType(val label: String, val gradient: List<Color>) {
    CYBER_NEON_GRID("Cyber Neon Pulse", listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF312E81))),
    SAKURA_DRIFT("Sakura Bloom Drift", listOf(Color(0xFFFDE8E8), Color(0xFFFBCFE8), Color(0xFFF472B6))),
    NEBULA_COSMOS("Celestial Nebula Cosmos", listOf(Color(0xFF1E1B4B), Color(0xFF581C87), Color(0xFF831843))),
    MINIMAL_AURORA("Minimalist White Aurora", listOf(Color(0xFFF8FAFC), Color(0xFFE2E8F0), Color(0xFFCBD5E1)))
}

data class AdminAnnouncement(
    val id: String,
    val title: String,
    val message: String,
    val authorRole: String = "Developer [System]",
    val timestamp: String = "Baru saja",
    val isUrgent: Boolean = false
)
