package com.example

import com.example.data.model.PetRarity
import com.example.data.model.UserRole
import com.example.data.repository.MoppiRepository
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testAnimePetRosterHasOver100Pets() {
        val repo = MoppiRepository()
        val pets = repo.petsList.value
        assertTrue("Pet roster must have at least 100 pets", pets.size >= 100)
    }

    @Test
    fun testAllSixRaritiesAreRepresented() {
        val repo = MoppiRepository()
        val pets = repo.petsList.value
        val rarities = pets.map { it.rarity }.toSet()
        assertEquals(6, rarities.size)
        assertTrue(rarities.contains(PetRarity.GALAXY))
        assertTrue(rarities.contains(PetRarity.MYTHICAL))
        assertTrue(rarities.contains(PetRarity.LEGEND))
        assertTrue(rarities.contains(PetRarity.EPIC))
        assertTrue(rarities.contains(PetRarity.RARE))
        assertTrue(rarities.contains(PetRarity.COMMON))
    }

    @Test
    fun testFiftyLanguagesSupported() {
        val repo = MoppiRepository()
        val languages = repo.supportedLanguages
        assertTrue("MoppiTV must support at least 50 languages", languages.size >= 50)
    }

    @Test
    fun testRoleVerificationChecklists() {
        assertTrue(UserRole.DEVELOPER.hasRedChecklist)
        assertTrue(UserRole.ADMIN.hasPurpleChecklist)
        assertFalse(UserRole.WARGA.hasRedChecklist)
    }

    @Test
    fun testGachaSpinDeduction() {
        val repo = MoppiRepository()
        val initialGems = repo.userProfile.value.moppiGems
        val result = repo.spinGacha(isTenPull = false)
        assertEquals(1, result.size)
        assertEquals(initialGems - 100, repo.userProfile.value.moppiGems)
    }

    @Test
    fun testAnimeScraperApiCreation() {
        val scraper = com.example.data.api.AnimeScraperApi.create()
        assertNotNull(scraper)
    }

    @Test
    fun testAnimeShortHasVideoStreamUrl() {
        val repo = MoppiRepository()
        val shorts = repo.animeShorts.value
        assertTrue(shorts.isNotEmpty())
        assertTrue(shorts.all { it.videoStreamUrl.isNotBlank() })
    }
}
