package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class PetRarity(val label: String, val color: Color, val dropRate: Double) {
    GALAXY("Galaxy", RarityGalaxy, 0.01),
    MYTHICAL("Mythical", RarityMythical, 0.04),
    LEGEND("Legend", RarityLegend, 0.10),
    EPIC("Epic", RarityEpic, 0.20),
    RARE("Rare", RarityRare, 0.30),
    COMMON("Common", RarityCommon, 0.35)
}

data class AnimePet(
    val id: String,
    val name: String,
    val animeOrigin: String,
    val rarity: PetRarity,
    val quote: String,
    val powerBonus: String,
    val iconEmoji: String,
    val isEquipped: Boolean = false,
    val isOwned: Boolean = false,
    val level: Int = 1
)

data class CoolFrame(
    val id: String,
    val name: String,
    val description: String,
    val gradientColors: List<Color>,
    val borderThicknessDp: Int = 3,
    val glowColor: Color,
    val isOwned: Boolean = false,
    val isEquipped: Boolean = false,
    val gemCost: Int = 150
)
