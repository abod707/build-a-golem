package com.artificer.buildagolem.model

object GolemDatabase {

    val allParts: List<GolemPart> = listOf(
        // === CORES (HEART) ===
        GolemPart(
            id = "core_clay_heart",
            name = "Clay Hearth Core",
            slot = PartSlot.CORE,
            element = GolemElement.EARTH,
            rarity = PartRarity.COMMON,
            hpBonus = 80,
            armorBonus = 5,
            attackBonus = 5,
            speedBonus = 0.0f,
            specialDescription = "Steady life pulse that gradually mends small cracks.",
            costGold = 0, costClay = 0, costOre = 0, costCrystal = 0
        ),
        GolemPart(
            id = "core_granite_engine",
            name = "Granite Dynamo",
            slot = PartSlot.CORE,
            element = GolemElement.STONE,
            rarity = PartRarity.RARE,
            hpBonus = 160,
            armorBonus = 15,
            attackBonus = 8,
            speedBonus = -0.1f,
            specialDescription = "Unshakeable stone core granting heavy physical stability.",
            costGold = 150, costClay = 30, costOre = 15, costCrystal = 4
        ),
        GolemPart(
            id = "core_pyre_spark",
            name = "Pyre Flame Heart",
            slot = PartSlot.CORE,
            element = GolemElement.FIRE,
            rarity = PartRarity.EPIC,
            hpBonus = 120,
            armorBonus = 10,
            attackBonus = 24,
            speedBonus = 0.2f,
            specialDescription = "Overheats the golem frame, scorching attackers with flame retaliation.",
            costGold = 320, costClay = 40, costOre = 30, costCrystal = 12
        ),
        GolemPart(
            id = "core_frost_crystal",
            name = "Glacial Core",
            slot = PartSlot.CORE,
            element = GolemElement.ICE,
            rarity = PartRarity.RARE,
            hpBonus = 140,
            armorBonus = 20,
            attackBonus = 12,
            speedBonus = 0.0f,
            specialDescription = "Freezes moisture in the air to coat the golem in crystalline ice armor.",
            costGold = 220, costClay = 30, costOre = 20, costCrystal = 8
        ),
        GolemPart(
            id = "core_arcane_singularity",
            name = "Aether Singularity",
            slot = PartSlot.CORE,
            element = GolemElement.ARCANE,
            rarity = PartRarity.LEGENDARY,
            hpBonus = 220,
            armorBonus = 25,
            attackBonus = 40,
            speedBonus = 0.4f,
            specialDescription = "Pure cosmic reactor allowing instant overdrive and destructive beams.",
            costGold = 600, costClay = 60, costOre = 50, costCrystal = 25
        ),

        // === HEADS (VISAGES) ===
        GolemPart(
            id = "head_clay_mask",
            name = "Earthen Visage",
            slot = PartSlot.HEAD,
            element = GolemElement.EARTH,
            rarity = PartRarity.COMMON,
            hpBonus = 40,
            armorBonus = 4,
            attackBonus = 4,
            speedBonus = 0.0f,
            specialDescription = "Carved clay mask with calm ember eyes.",
            costGold = 0, costClay = 0, costOre = 0, costCrystal = 0
        ),
        GolemPart(
            id = "head_iron_helm",
            name = "Steel Bastion Visor",
            slot = PartSlot.HEAD,
            element = GolemElement.IRON,
            rarity = PartRarity.RARE,
            hpBonus = 70,
            armorBonus = 18,
            attackBonus = 8,
            speedBonus = 0.0f,
            specialDescription = "Riveted plate helm protecting internal mechanisms from critical strikes.",
            costGold = 160, costClay = 20, costOre = 25, costCrystal = 4
        ),
        GolemPart(
            id = "head_cyclops_focus",
            name = "Cyclopean Arcane Eye",
            slot = PartSlot.HEAD,
            element = GolemElement.ARCANE,
            rarity = PartRarity.EPIC,
            hpBonus = 60,
            armorBonus = 12,
            attackBonus = 26,
            speedBonus = 0.2f,
            specialDescription = "Single glowing lens that focuses mystic rays on enemy weak spots.",
            costGold = 340, costClay = 25, costOre = 20, costCrystal = 15
        ),
        GolemPart(
            id = "head_magma_horns",
            name = "Infernal Ram Crest",
            slot = PartSlot.HEAD,
            element = GolemElement.FIRE,
            rarity = PartRarity.EPIC,
            hpBonus = 90,
            armorBonus = 14,
            attackBonus = 28,
            speedBonus = 0.1f,
            specialDescription = "Burning volcanic horns that deal fiery headbutt impact damage.",
            costGold = 350, costClay = 30, costOre = 35, costCrystal = 12
        ),
        GolemPart(
            id = "head_storm_crown",
            name = "Storm Conduit Crown",
            slot = PartSlot.HEAD,
            element = GolemElement.LIGHTNING,
            rarity = PartRarity.LEGENDARY,
            hpBonus = 110,
            armorBonus = 16,
            attackBonus = 38,
            speedBonus = 0.5f,
            specialDescription = "Crackling spikes that discharge electric shocks to nearby enemies.",
            costGold = 580, costClay = 40, costOre = 45, costCrystal = 22
        ),

        // === TORSOS ===
        GolemPart(
            id = "torso_clay_bulk",
            name = "Packed Clay Body",
            slot = PartSlot.TORSO,
            element = GolemElement.EARTH,
            rarity = PartRarity.COMMON,
            hpBonus = 100,
            armorBonus = 6,
            attackBonus = 4,
            speedBonus = 0.0f,
            specialDescription = "Stout clay torso capable of absorbing shocks.",
            costGold = 0, costClay = 0, costOre = 0, costCrystal = 0
        ),
        GolemPart(
            id = "torso_granite_slab",
            name = "Monolithic Granite Slab",
            slot = PartSlot.TORSO,
            element = GolemElement.STONE,
            rarity = PartRarity.RARE,
            hpBonus = 200,
            armorBonus = 28,
            attackBonus = 10,
            speedBonus = -0.2f,
            specialDescription = "Solid carved boulder body with impenetrable density.",
            costGold = 200, costClay = 40, costOre = 30, costCrystal = 5
        ),
        GolemPart(
            id = "torso_iron_plating",
            name = "Reinforced Iron Bastion",
            slot = PartSlot.TORSO,
            element = GolemElement.IRON,
            rarity = PartRarity.EPIC,
            hpBonus = 180,
            armorBonus = 35,
            attackBonus = 16,
            speedBonus = 0.0f,
            specialDescription = "Heavy interlocking iron scales that deflect blades and arrows.",
            costGold = 380, costClay = 30, costOre = 50, costCrystal = 10
        ),
        GolemPart(
            id = "torso_volcanic_carapace",
            name = "Volcanic Magma Crust",
            slot = PartSlot.TORSO,
            element = GolemElement.FIRE,
            rarity = PartRarity.EPIC,
            hpBonus = 160,
            armorBonus = 22,
            attackBonus = 30,
            speedBonus = 0.1f,
            specialDescription = "Lava flows beneath glowing basalt plates, releasing explosive heat.",
            costGold = 410, costClay = 40, costOre = 45, costCrystal = 16
        ),
        GolemPart(
            id = "torso_aether_shield",
            name = "Prismatic Crystal Shell",
            slot = PartSlot.TORSO,
            element = GolemElement.ARCANE,
            rarity = PartRarity.LEGENDARY,
            hpBonus = 280,
            armorBonus = 45,
            attackBonus = 45,
            speedBonus = 0.3f,
            specialDescription = "Resonating crystal geode body emitting protective arcane barrier pulses.",
            costGold = 700, costClay = 50, costOre = 60, costCrystal = 30
        ),

        // === ARMS (WEAPONRY & FISTS) ===
        GolemPart(
            id = "arms_clay_fists",
            name = "Heavy Clay Fists",
            slot = PartSlot.ARMS,
            element = GolemElement.EARTH,
            rarity = PartRarity.COMMON,
            hpBonus = 30,
            armorBonus = 4,
            attackBonus = 15,
            speedBonus = 0.0f,
            specialDescription = "Dense mud fists that hit with surprising force.",
            costGold = 0, costClay = 0, costOre = 0, costCrystal = 0
        ),
        GolemPart(
            id = "arms_iron_crushers",
            name = "Forged Iron Maulers",
            slot = PartSlot.ARMS,
            element = GolemElement.IRON,
            rarity = PartRarity.RARE,
            hpBonus = 50,
            armorBonus = 12,
            attackBonus = 32,
            speedBonus = -0.1f,
            specialDescription = "Enormous spiked iron fists that pulverize stone and armor.",
            costGold = 180, costClay = 20, costOre = 40, costCrystal = 4
        ),
        GolemPart(
            id = "arms_blazing_gauntlets",
            name = "Blazing Pyre Claws",
            slot = PartSlot.ARMS,
            element = GolemElement.FIRE,
            rarity = PartRarity.EPIC,
            hpBonus = 40,
            armorBonus = 8,
            attackBonus = 46,
            speedBonus = 0.3f,
            specialDescription = "White-hot claws that leave exploding trails of embers with every swipe.",
            costGold = 390, costClay = 25, costOre = 40, costCrystal = 15
        ),
        GolemPart(
            id = "arms_frost_pincers",
            name = "Glacial Slicers",
            slot = PartSlot.ARMS,
            element = GolemElement.ICE,
            rarity = PartRarity.RARE,
            hpBonus = 60,
            armorBonus = 14,
            attackBonus = 34,
            speedBonus = 0.1f,
            specialDescription = "Razor-sharp permafrost blades that chill and slow struck enemies.",
            costGold = 240, costClay = 25, costOre = 30, costCrystal = 8
        ),
        GolemPart(
            id = "arms_storm_hammers",
            name = "Thunderclap Mallets",
            slot = PartSlot.ARMS,
            element = GolemElement.LIGHTNING,
            rarity = PartRarity.LEGENDARY,
            hpBonus = 80,
            armorBonus = 15,
            attackBonus = 68,
            speedBonus = 0.4f,
            specialDescription = "Electrified warhammers causing chain-lightning shockwaves on impact.",
            costGold = 650, costClay = 45, costOre = 60, costCrystal = 26
        ),

        // === LEGS (LOCOMOTION) ===
        GolemPart(
            id = "legs_clay_pillars",
            name = "Clay Stumps",
            slot = PartSlot.LEGS,
            element = GolemElement.EARTH,
            rarity = PartRarity.COMMON,
            hpBonus = 50,
            armorBonus = 5,
            attackBonus = 2,
            speedBonus = 2.4f,
            specialDescription = "Thick clay legs providing steady forward momentum.",
            costGold = 0, costClay = 0, costOre = 0, costCrystal = 0
        ),
        GolemPart(
            id = "legs_granite_columns",
            name = "Granite Pillars",
            slot = PartSlot.LEGS,
            element = GolemElement.STONE,
            rarity = PartRarity.RARE,
            hpBonus = 120,
            armorBonus = 20,
            attackBonus = 6,
            speedBonus = 2.1f,
            specialDescription = "Ground-shaking stone pillars that cause tremors when jumping.",
            costGold = 170, costClay = 35, costOre = 25, costCrystal = 4
        ),
        GolemPart(
            id = "legs_hydraulic_pistons",
            name = "Steam Piston Struts",
            slot = PartSlot.LEGS,
            element = GolemElement.IRON,
            rarity = PartRarity.EPIC,
            hpBonus = 80,
            armorBonus = 15,
            attackBonus = 10,
            speedBonus = 3.6f,
            specialDescription = "High-pressure pneumatic brass joints allowing rapid dashing.",
            costGold = 360, costClay = 20, costOre = 50, costCrystal = 12
        ),
        GolemPart(
            id = "legs_magma_skates",
            name = "Molten Treads",
            slot = PartSlot.LEGS,
            element = GolemElement.FIRE,
            rarity = PartRarity.EPIC,
            hpBonus = 90,
            armorBonus = 12,
            attackBonus = 18,
            speedBonus = 3.4f,
            specialDescription = "Superheated basalt rollers that ignite the earth beneath the golem.",
            costGold = 380, costClay = 30, costOre = 40, costCrystal = 14
        ),
        GolemPart(
            id = "legs_hover_orbiters",
            name = "Celestial Levitation Runes",
            slot = PartSlot.LEGS,
            element = GolemElement.ARCANE,
            rarity = PartRarity.LEGENDARY,
            hpBonus = 140,
            armorBonus = 25,
            attackBonus = 24,
            speedBonus = 4.2f,
            specialDescription = "Floating anti-gravity glyph rings that effortlessly glide above dangers.",
            costGold = 640, costClay = 40, costOre = 55, costCrystal = 28
        )
    )

    private val partsMap: Map<String, GolemPart> = allParts.associateBy { it.id }

    fun getPart(id: String): GolemPart {
        return partsMap[id] ?: allParts.first { it.slot == PartSlot.CORE }
    }

    fun getPartsForSlot(slot: PartSlot): List<GolemPart> {
        return allParts.filter { it.slot == slot }
    }

    fun calculateStats(config: GolemConfiguration): GolemStats {
        val core = getPart(config.coreId)
        val head = getPart(config.headId)
        val torso = getPart(config.torsoId)
        val arms = getPart(config.armsId)
        val legs = getPart(config.legsId)

        val parts = listOf(core, head, torso, arms, legs)

        var totalHp = 200 + parts.sumOf { it.hpBonus }
        var totalArmor = 10 + parts.sumOf { it.armorBonus }
        var totalAttack = 15 + parts.sumOf { it.attackBonus }
        var baseSpeed = legs.speedBonus + parts.filter { it != legs }.sumOf { it.speedBonus.toDouble() }.toFloat()
        if (baseSpeed < 1.6f) baseSpeed = 1.6f

        // Synergy calculation
        val elementCounts = parts.groupingBy { it.element }.eachCount()
        val synergies = mutableListOf<String>()

        elementCounts.forEach { (elem, count) ->
            if (count >= 3) {
                when (elem) {
                    GolemElement.EARTH -> {
                        totalHp = (totalHp * 1.25f).toInt()
                        synergies.add("Earth Harmony (3+): +25% Max HP & Passive HP Regen")
                    }
                    GolemElement.STONE -> {
                        totalArmor = (totalArmor * 1.35f).toInt()
                        synergies.add("Granite Bastion (3+): +35% Armor & Unstoppable Mass")
                    }
                    GolemElement.IRON -> {
                        totalAttack = (totalAttack * 1.25f).toInt()
                        synergies.add("Steel Vanguard (3+): +25% Physical Strike Damage")
                    }
                    GolemElement.FIRE -> {
                        totalAttack = (totalAttack * 1.30f).toInt()
                        synergies.add("Infernal Fury (3+): +30% Attack & Explosive Burst")
                    }
                    GolemElement.ICE -> {
                        totalArmor = (totalArmor * 1.20f).toInt()
                        synergies.add("Glacial Shroud (3+): +20% Armor & Slow Attacking Foes")
                    }
                    GolemElement.LIGHTNING -> {
                        baseSpeed *= 1.3f
                        synergies.add("Storm Catalyst (3+): +30% Movement Speed & Arc Stuns")
                    }
                    GolemElement.ARCANE -> {
                        totalHp = (totalHp * 1.15f).toInt()
                        totalAttack = (totalAttack * 1.20f).toInt()
                        synergies.add("Cosmic Resonance (3+): +15% HP, +20% Attack, Faster Skills")
                    }
                }
            }
        }

        // Most dominant element
        val dominantElement = elementCounts.maxByOrNull { it.value }?.key ?: core.element

        return GolemStats(
            maxHp = totalHp,
            armor = totalArmor,
            attack = totalAttack,
            speed = baseSpeed,
            primaryElement = dominantElement,
            activeSynergies = synergies
        )
    }

    val expeditionStages: List<ExpeditionStage> = listOf(
        ExpeditionStage(
            stageNumber = 1,
            title = "Whispering Caverns",
            subtitle = "Clear the corrupted imps threatening the forge outskirts",
            recommendedPower = 80,
            wavesCount = 3,
            bossType = null,
            goldReward = 120, clayReward = 35, oreReward = 15, crystalReward = 4
        ),
        ExpeditionStage(
            stageNumber = 2,
            title = "Clockwork Mines",
            subtitle = "Disarm rogue steam beetles and iron constructs",
            recommendedPower = 150,
            wavesCount = 4,
            bossType = null,
            goldReward = 200, clayReward = 45, oreReward = 30, crystalReward = 8
        ),
        ExpeditionStage(
            stageNumber = 3,
            title = "Molten Caldera",
            subtitle = "Confront the fire fiends and Ignis the Magma Colossus",
            recommendedPower = 280,
            wavesCount = 4,
            bossType = EnemyType.BOSS_MOLTEN_OVERLORD,
            goldReward = 380, clayReward = 60, oreReward = 45, crystalReward = 16
        ),
        ExpeditionStage(
            stageNumber = 4,
            title = "Aether Spire Ruins",
            subtitle = "Shatter the void shades and Aetherius Prime",
            recommendedPower = 420,
            wavesCount = 5,
            bossType = EnemyType.BOSS_ARCANE_TITAN,
            goldReward = 600, clayReward = 80, oreReward = 70, crystalReward = 30
        )
    )
}
