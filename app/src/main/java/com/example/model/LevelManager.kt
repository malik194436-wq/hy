package com.example.model

import com.example.data.GameStorage
import kotlin.math.min

/**
 * Status of a level node on the cosmic map.
 */
enum class LevelStatus {
    LOCKED,
    UNLOCKED,
    COMPLETED
}

/**
 * Data model for an individual level node within the 5000-level system.
 */
data class LevelNodeData(
    val levelNumber: Int,
    val chapterNumber: Int,
    val status: LevelStatus,
    val stars: Int, // 0 to 3
    val startingBalls: Int,
    val difficultyTitle: String,
    val description: String,
    val targetTurnsFor3Stars: Int = 20,
    val targetTurnsFor2Stars: Int = 30
)

/**
 * Data model for a chapter/page grouping (6 levels per screen/chapter).
 */
data class ChapterData(
    val chapterNumber: Int,
    val title: String,
    val startLevel: Int,
    val endLevel: Int,
    val isUnlocked: Boolean,
    val levels: List<LevelNodeData>
)

/**
 * Scalable, data-driven level management system supporting 5000 dynamic levels.
 * Avoids hardcoding individual levels by deterministically calculating level stats,
 * chapter groupings, starting balls, and unlocking progression.
 */
class LevelManager(
    private val storage: GameStorage,
    val totalLevels: Int = 5000,
    val levelsPerChapter: Int = 6
) {
    val totalChapters: Int = (totalLevels + levelsPerChapter - 1) / levelsPerChapter

    /**
     * Retrieves the player's highest unlocked stage from persistent storage.
     */
    val highestUnlockedLevel: Int
        get() = storage.highestStageUnlocked.coerceIn(1, totalLevels)

    /**
     * Determines whether a specific level is currently accessible.
     */
    fun isLevelUnlocked(levelNumber: Int): Boolean {
        return levelNumber <= highestUnlockedLevel
    }

    /**
     * Determines whether a specific level has already been cleared.
     */
    fun isLevelCompleted(levelNumber: Int): Boolean {
        return levelNumber < highestUnlockedLevel
    }

    /**
     * Retrieves the star count (0 to 3) achieved on a given stage.
     */
    fun getStars(levelNumber: Int): Int {
        return if (isLevelCompleted(levelNumber)) {
            storage.getStageStars(levelNumber).coerceIn(1, 3)
        } else {
            0
        }
    }

    /**
     * Calculates the starting ball count for a given level.
     * Starts at a generous 60 balls and scales up to 150 balls for ultra-high stages.
     */
    fun calculateStartingBalls(levelNumber: Int): Int {
        return (60 + (levelNumber / 50)).coerceIn(60, 150)
    }

    /**
     * Determines the cosmic theme and difficulty tier based on level range.
     */
    fun calculateDifficulty(levelNumber: Int): String {
        return when {
            levelNumber <= 50 -> "Novice Galaxy"
            levelNumber <= 200 -> "Nebula Voyage"
            levelNumber <= 500 -> "Cosmic Storm"
            levelNumber <= 1000 -> "Stellar Abyss"
            levelNumber <= 2500 -> "Supernova Core"
            else -> "Infinity Master"
        }
    }

    /**
     * Dynamically generates the data for a given level node.
     */
    fun getLevelData(levelNumber: Int): LevelNodeData {
        val safeLevel = levelNumber.coerceIn(1, totalLevels)
        val status = when {
            safeLevel < highestUnlockedLevel -> LevelStatus.COMPLETED
            safeLevel == highestUnlockedLevel -> LevelStatus.UNLOCKED
            else -> LevelStatus.LOCKED
        }
        val stars = getStars(safeLevel)
        val chapter = ((safeLevel - 1) / levelsPerChapter) + 1
        val startingBalls = calculateStartingBalls(safeLevel)
        val difficulty = calculateDifficulty(safeLevel)
        val description = "$startingBalls Starting Balls • Aim & Destroy Bricks"

        return LevelNodeData(
            levelNumber = safeLevel,
            chapterNumber = chapter,
            status = status,
            stars = stars,
            startingBalls = startingBalls,
            difficultyTitle = difficulty,
            description = description,
            targetTurnsFor3Stars = 15 + (safeLevel % 15),
            targetTurnsFor2Stars = 25 + (safeLevel % 15)
        )
    }

    /**
     * Retrieves all level nodes belonging to a specific chapter index (0-indexed).
     */
    fun getChapter(chapterIndex: Int): ChapterData {
        val safeIndex = chapterIndex.coerceIn(0, totalChapters - 1)
        val chNum = safeIndex + 1
        val start = safeIndex * levelsPerChapter + 1
        val end = min(start + levelsPerChapter - 1, totalLevels)
        val levels = (start..end).map { getLevelData(it) }
        val isUnlocked = start <= highestUnlockedLevel

        return ChapterData(
            chapterNumber = chNum,
            title = "CHAPTER $chNum",
            startLevel = start,
            endLevel = end,
            isUnlocked = isUnlocked,
            levels = levels
        )
    }

    /**
     * Returns the 0-indexed chapter index for a given level number.
     */
    fun getChapterIndexForLevel(levelNumber: Int): Int {
        val safeLevel = levelNumber.coerceIn(1, totalLevels)
        return ((safeLevel - 1) / levelsPerChapter).coerceIn(0, totalChapters - 1)
    }

    /**
     * Total stars collected by the player across all completed levels.
     */
    fun getTotalStarsEarned(): Int {
        var total = 0
        val maxToCheck = min(highestUnlockedLevel - 1, totalLevels)
        for (i in 1..maxToCheck) {
            total += storage.getStageStars(i).coerceIn(1, 3)
        }
        return total
    }
}
