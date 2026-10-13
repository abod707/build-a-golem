package com.artificer.buildagolem.model

import org.json.JSONArray
import org.json.JSONObject

enum class GolemElement(val displayName: String, val colorHex: Int, val description: String) {
    EARTH("Clay & Earth", 0xFFC27D38.toInt(), "High resilience, natural health regeneration"),
    STONE("Granite & Slate", 0xFF8D99AE.toInt(), "Massive armor and overwhelming knockback"),
    IRON("Forged Steel", 0xFFD8D8D8.toInt(), "Razor physical damage and critical stun strikes"),
    FIRE("Molten Magma", 0xFFFF5722.toInt(), "Deals blazing burn damage over time and explosive impacts"),
    ICE("Glacial Frost", 0xFF48CAE4.toInt(), "Slows enemy attacks and shatters brittle foes"),
    LIGHTNING("Storm Conduit", 0xFFFFD166.toInt(), "Rapid movement and chain lightning arcing between foes"),
    ARCANE("Cosmic Crystal", 0xFF9D4EDD.toInt(), "Shield barriers, piercing lasers, and fast ability cooldowns")
}

enum class PartSlot(val displayName: String) {
    CORE("Heart Core"),
    HEAD("Visage"),
    TORSO("Torso"),
    ARMS("Arms & Fists"),
    LEGS("Legs & Base")
}

enum class PartRarity(val displayName: String, val colorHex: Int, val statMultiplier: Float) {
    COMMON("Apprentice", 0xFFA09DB5.toInt(), 1.0f),
    RARE("Artisan", 0xFF06D6A0.toInt(), 1.25f),
    EPIC("Masterwork", 0xFF118AB2.toInt(), 1.6f),
    LEGENDARY("Mythic", 0xFFFFD166.toInt(), 2.1f)
}

data class GolemPart(
    val id: String,
    val name: String,
    val slot: PartSlot,
    val element: GolemElement,
    val rarity: PartRarity,
    val hpBonus: Int,
    val armorBonus: Int,
    val attackBonus: Int,
    val speedBonus: Float,
    val specialDescription: String,
    val costGold: Int = 100,
    val costClay: Int = 10,
    val costOre: Int = 5,
    val costCrystal: Int = 2
) {
    fun toJsonObject(): JSONObject {
        val obj = JSONObject()
        obj.put("id", id)
        return obj
    }
}

data class GolemStats(
    val maxHp: Int,
    val armor: Int,
    val attack: Int,
    val speed: Float,
    val primaryElement: GolemElement,
    val activeSynergies: List<String>
)

data class PlayerResources(
    var gold: Int = 250,
    var clay: Int = 80,
    var ironOre: Int = 40,
    var crystals: Int = 15,
    var embers: Int = 5
) {
    fun toJsonObject(): JSONObject {
        val obj = JSONObject()
        obj.put("gold", gold)
        obj.put("clay", clay)
        obj.put("ironOre", ironOre)
        obj.put("crystals", crystals)
        obj.put("embers", embers)
        return obj
    }

    companion object {
        fun fromJsonObject(obj: JSONObject): PlayerResources {
            return PlayerResources(
                gold = obj.optInt("gold", 250),
                clay = obj.optInt("clay", 80),
                ironOre = obj.optInt("ironOre", 40),
                crystals = obj.optInt("crystals", 15),
                embers = obj.optInt("embers", 5)
            )
        }
    }
}

data class GolemConfiguration(
    var name: String = "Terra Titan",
    var coreId: String = "core_clay_heart",
    var headId: String = "head_clay_mask",
    var torsoId: String = "torso_clay_bulk",
    var armsId: String = "arms_clay_fists",
    var legsId: String = "legs_clay_pillars"
) {
    fun toJsonObject(): JSONObject {
        val obj = JSONObject()
        obj.put("name", name)
        obj.put("coreId", coreId)
        obj.put("headId", headId)
        obj.put("torsoId", torsoId)
        obj.put("armsId", armsId)
        obj.put("legsId", legsId)
        return obj
    }

    companion object {
        fun fromJsonObject(obj: JSONObject): GolemConfiguration {
            return GolemConfiguration(
                name = obj.optString("name", "Terra Titan"),
                coreId = obj.optString("coreId", "core_clay_heart"),
                headId = obj.optString("headId", "head_clay_mask"),
                torsoId = obj.optString("torsoId", "torso_clay_bulk"),
                armsId = obj.optString("armsId", "arms_clay_fists"),
                legsId = obj.optString("legsId", "legs_clay_pillars")
            )
        }
    }
}

enum class EnemyType(
    val displayName: String,
    val baseHp: Int,
    val baseAttack: Int,
    val speed: Float,
    val colorHex: Int,
    val element: GolemElement,
    val isBoss: Boolean = false,
    val scoreValue: Int = 100
) {
    GOBLIN_SCAVENGER("Corrupted Imp", 70, 10, 2.8f, 0xFF70A040.toInt(), GolemElement.EARTH, false, 50),
    CLOCKWORK_DRONE("Clockwork Beetle", 110, 14, 2.2f, 0xFFC0A060.toInt(), GolemElement.IRON, false, 80),
    SHADOW_WISP("Void Shade", 85, 18, 3.2f, 0xFF7A4495.toInt(), GolemElement.ARCANE, false, 110),
    MAGMA_HOUND("Magma Hound", 140, 22, 2.9f, 0xFFE65100.toInt(), GolemElement.FIRE, false, 140),
    FROST_WRAITH("Frost Wraith", 130, 16, 2.0f, 0xFF29B6F6.toInt(), GolemElement.ICE, false, 130),
    IRON_COLOSSUS("Clockwork Juggernaut", 320, 28, 1.4f, 0xFF78909C.toInt(), GolemElement.IRON, false, 250),
    BOSS_MOLTEN_OVERLORD("Ignis the Magma Colossus", 1500, 42, 1.6f, 0xFFFF3D00.toInt(), GolemElement.FIRE, true, 1000),
    BOSS_ARCANE_TITAN("Aetherius Prime", 2200, 52, 1.8f, 0xFFAB47BC.toInt(), GolemElement.ARCANE, true, 2000)
}

data class ExpeditionStage(
    val stageNumber: Int,
    val title: String,
    val subtitle: String,
    val recommendedPower: Int,
    val wavesCount: Int,
    val bossType: EnemyType?,
    val goldReward: Int,
    val clayReward: Int,
    val oreReward: Int,
    val crystalReward: Int
)
