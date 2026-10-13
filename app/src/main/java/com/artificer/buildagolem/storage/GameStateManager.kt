package com.artificer.buildagolem.storage

import android.content.Context
import android.content.SharedPreferences
import com.artificer.buildagolem.model.GolemConfiguration
import com.artificer.buildagolem.model.GolemDatabase
import com.artificer.buildagolem.model.GolemPart
import com.artificer.buildagolem.model.PlayerResources
import org.json.JSONArray
import org.json.JSONObject

class GameStateManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("build_a_golem_prefs", Context.MODE_PRIVATE)

    var resources: PlayerResources = PlayerResources()
        private set

    var currentGolem: GolemConfiguration = GolemConfiguration()
        private set

    val unlockedPartIds: MutableSet<String> = mutableSetOf()

    var highestStageCleared: Int = 0
        private set

    var endlessHighScore: Int = 0
        private set

    var soundEnabled: Boolean = true
    var hapticsEnabled: Boolean = true
    var damageNumbersEnabled: Boolean = true

    init {
        loadState()
    }

    private fun loadState() {
        soundEnabled = prefs.getBoolean("sound_enabled", true)
        hapticsEnabled = prefs.getBoolean("haptics_enabled", true)
        damageNumbersEnabled = prefs.getBoolean("damage_numbers_enabled", true)
        highestStageCleared = prefs.getInt("highest_stage_cleared", 0)
        endlessHighScore = prefs.getInt("endless_high_score", 0)

        // Resources
        val resJsonStr = prefs.getString("resources_json", null)
        resources = if (resJsonStr != null) {
            try {
                PlayerResources.fromJsonObject(JSONObject(resJsonStr))
            } catch (e: Exception) {
                PlayerResources()
            }
        } else {
            PlayerResources()
        }

        // Unlocked parts
        val unlockedJsonStr = prefs.getString("unlocked_parts_json", null)
        unlockedPartIds.clear()
        if (unlockedJsonStr != null) {
            try {
                val arr = JSONArray(unlockedJsonStr)
                for (i in 0 until arr.length()) {
                    unlockedPartIds.add(arr.getString(i))
                }
            } catch (_: Exception) {}
        }

        // Ensure default starter parts are always unlocked
        val defaultParts = listOf(
            "core_clay_heart", "head_clay_mask", "torso_clay_bulk", "arms_clay_fists", "legs_clay_pillars",
            "core_granite_engine", "arms_iron_crushers" // starter extra parts so player can customize right away!
        )
        unlockedPartIds.addAll(defaultParts)

        // Equipped configuration
        val configJsonStr = prefs.getString("equipped_golem_json", null)
        currentGolem = if (configJsonStr != null) {
            try {
                GolemConfiguration.fromJsonObject(JSONObject(configJsonStr))
            } catch (e: Exception) {
                GolemConfiguration()
            }
        } else {
            GolemConfiguration()
        }
    }

    fun saveState() {
        val editor = prefs.edit()
        editor.putBoolean("sound_enabled", soundEnabled)
        editor.putBoolean("haptics_enabled", hapticsEnabled)
        editor.putBoolean("damage_numbers_enabled", damageNumbersEnabled)
        editor.putInt("highest_stage_cleared", highestStageCleared)
        editor.putInt("endless_high_score", endlessHighScore)

        editor.putString("resources_json", resources.toJsonObject().toString())

        val arr = JSONArray()
        unlockedPartIds.forEach { arr.put(it) }
        editor.putString("unlocked_parts_json", arr.toString())

        editor.putString("equipped_golem_json", currentGolem.toJsonObject().toString())
        editor.apply()
    }

    fun canAfford(part: GolemPart): Boolean {
        return resources.gold >= part.costGold &&
                resources.clay >= part.costClay &&
                resources.ironOre >= part.costOre &&
                resources.crystals >= part.costCrystal
    }

    fun buyAndUnlockPart(part: GolemPart): Boolean {
        if (!canAfford(part)) return false
        resources.gold -= part.costGold
        resources.clay -= part.costClay
        resources.ironOre -= part.costOre
        resources.crystals -= part.costCrystal
        unlockedPartIds.add(part.id)
        saveState()
        return true
    }

    fun unlockPartFree(partId: String) {
        unlockedPartIds.add(partId)
        saveState()
    }

    fun updateGolemConfig(newConfig: GolemConfiguration) {
        currentGolem = newConfig
        saveState()
    }

    fun recordStageCompletion(stageNumber: Int, gold: Int, clay: Int, ore: Int, crystal: Int) {
        if (stageNumber > highestStageCleared) {
            highestStageCleared = stageNumber
        }
        resources.gold += gold
        resources.clay += clay
        resources.ironOre += ore
        resources.crystals += crystal
        saveState()
    }

    fun recordEndlessScore(score: Int, goldReward: Int, clayReward: Int, oreReward: Int, crystalReward: Int) {
        if (score > endlessHighScore) {
            endlessHighScore = score
        }
        resources.gold += goldReward
        resources.clay += clayReward
        resources.ironOre += oreReward
        resources.crystals += crystalReward
        saveState()
    }

    fun addResources(gold: Int, clay: Int, ore: Int, crystal: Int, embers: Int = 0) {
        resources.gold += gold
        resources.clay += clay
        resources.ironOre += ore
        resources.crystals += crystal
        resources.embers += embers
        saveState()
    }

    fun resetProgress() {
        prefs.edit().clear().apply()
        loadState()
        saveState()
    }
}
