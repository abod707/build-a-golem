package com.artificer.buildagolem.game

import android.graphics.RectF
import com.artificer.buildagolem.audio.GameAudio
import com.artificer.buildagolem.model.*
import kotlin.math.*

data class CombatEntity(
    var id: String,
    var name: String,
    var isPlayer: Boolean,
    var x: Float,
    var y: Float,
    var vx: Float = 0f,
    var maxHp: Int,
    var currentHp: Int,
    var attack: Int,
    var armor: Int,
    var speed: Float,
    var width: Float,
    var height: Float,
    var colorHex: Int,
    var element: GolemElement,
    var isBoss: Boolean = false,
    var hitFlashTimer: Float = 0f,
    var attackCooldown: Float = 0f,
    var stateTime: Float = 0f,
    var isBlocking: Boolean = false
) {
    val bounds: RectF
        get() = RectF(x - width / 2f, y - height, x + width / 2f, y)
}

data class Projectile(
    var x: Float,
    var y: Float,
    var vx: Float,
    var damage: Int,
    var fromPlayer: Boolean,
    var element: GolemElement,
    var radius: Float = 12f,
    var life: Float = 2.5f
)

data class FloatingText(
    var text: String,
    var x: Float,
    var y: Float,
    var vy: Float = -1.8f,
    var color: Int,
    var life: Float = 1.0f
)

data class CombatParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var color: Int,
    var size: Float,
    var life: Float,
    var maxLife: Float
)

class BattleEngine(
    val playerConfig: GolemConfiguration,
    val stage: ExpeditionStage?,
    val isEndless: Boolean = false
) {
    val playerStats: GolemStats = GolemDatabase.calculateStats(playerConfig)

    val player: CombatEntity = CombatEntity(
        id = "player_golem",
        name = playerConfig.name,
        isPlayer = true,
        x = 180f,
        y = 0f, // will set to ground in view
        maxHp = playerStats.maxHp,
        currentHp = playerStats.maxHp,
        attack = playerStats.attack,
        armor = playerStats.armor,
        speed = playerStats.speed,
        width = 90f,
        height = 160f,
        colorHex = playerStats.primaryElement.colorHex,
        element = playerStats.primaryElement
    )

    val enemies = mutableListOf<CombatEntity>()
    val projectiles = mutableListOf<Projectile>()
    val floatingTexts = mutableListOf<FloatingText>()
    val particles = mutableListOf<CombatParticle>()

    var currentWave: Int = 1
    val maxWaves: Int = stage?.wavesCount ?: 999
    var waveSpawnTimer: Float = 1.0f
    var isVictory: Boolean = false
    var isDefeat: Boolean = false
    var score: Int = 0

    // Player skill cooldowns and energy
    var overdriveEnergy: Float = 30f // 0 to 100
    var coreSkillCooldown: Float = 0f
    var shieldCooldown: Float = 0f
    var punchCooldown: Float = 0f
    var screenShakeAmount: Float = 0f

    // Resource spoils collected during battle
    var collectedGold: Int = 0
    var collectedClay: Int = 0
    var collectedOre: Int = 0
    var collectedCrystals: Int = 0

    fun update(dt: Float, arenaWidth: Float, groundY: Float) {
        if (isVictory || isDefeat) return

        player.y = groundY
        screenShakeAmount = (screenShakeAmount - dt * 15f).coerceAtLeast(0f)

        // Decrement cooldowns
        punchCooldown = (punchCooldown - dt).coerceAtLeast(0f)
        coreSkillCooldown = (coreSkillCooldown - dt).coerceAtLeast(0f)
        shieldCooldown = (shieldCooldown - dt).coerceAtLeast(0f)
        player.attackCooldown = (player.attackCooldown - dt).coerceAtLeast(0f)
        player.hitFlashTimer = (player.hitFlashTimer - dt).coerceAtLeast(0f)
        player.stateTime += dt

        // Player natural regen from Earth synergy
        if (playerStats.activeSynergies.any { it.contains("Earth Harmony") }) {
            if (player.stateTime % 1.5f < dt && player.currentHp < player.maxHp) {
                val heal = (player.maxHp * 0.02f).toInt().coerceAtLeast(2)
                player.currentHp = (player.currentHp + heal).coerceAtMost(player.maxHp)
                floatingTexts.add(FloatingText("+$heal", player.x, player.y - 140f, -1.5f, 0xFF06D6A0.toInt()))
            }
        }

        // Wave Spawning
        if (enemies.isEmpty()) {
            waveSpawnTimer -= dt
            if (waveSpawnTimer <= 0f) {
                if (currentWave > maxWaves && !isEndless) {
                    isVictory = true
                    GameAudio.playVictory()
                    collectedGold += stage?.goldReward ?: 100
                    collectedClay += stage?.clayReward ?: 20
                    collectedOre += stage?.oreReward ?: 15
                    collectedCrystals += stage?.crystalReward ?: 5
                    return
                }
                spawnWave(arenaWidth, groundY)
            }
        }

        // Update Enemies
        val enemyIt = enemies.iterator()
        while (enemyIt.hasNext()) {
            val enemy = enemyIt.next()
            enemy.y = groundY
            enemy.stateTime += dt
            enemy.hitFlashTimer = (enemy.hitFlashTimer - dt).coerceAtLeast(0f)
            enemy.attackCooldown = (enemy.attackCooldown - dt).coerceAtLeast(0f)

            // Move toward player
            val distToPlayer = player.x - enemy.x
            val attackRange = if (enemy.element == GolemElement.ARCANE) 280f else 85f

            if (abs(distToPlayer) > attackRange) {
                val dir = if (distToPlayer > 0) 1f else -1f
                enemy.x += dir * enemy.speed * dt * 50f
            } else {
                // Enemy Attacks
                if (enemy.attackCooldown <= 0f) {
                    performEnemyAttack(enemy)
                }
            }

            // Check enemy death
            if (enemy.currentHp <= 0) {
                enemyIt.remove()
                score += if (enemy.isBoss) 1500 else 120
                overdriveEnergy = (overdriveEnergy + 15f).coerceAtMost(100f)
                spawnDeathExplosion(enemy.x, enemy.y - enemy.height / 2f, enemy.colorHex)

                // Enemy loot drops
                collectedGold += if (enemy.isBoss) 100 else 18
                collectedClay += if (enemy.isBoss) 30 else 6
                collectedOre += if (enemy.isBoss) 20 else 4
                if (enemy.isBoss || Math.random() < 0.25) collectedCrystals += 2
            }
        }

        // Update Projectiles
        val projIt = projectiles.iterator()
        while (projIt.hasNext()) {
            val p = projIt.next()
            p.x += p.vx * dt
            p.life -= dt

            if (p.life <= 0f || p.x < 0 || p.x > arenaWidth + 200f) {
                projIt.remove()
                continue
            }

            // Projectile Collisions
            if (p.fromPlayer) {
                for (enemy in enemies) {
                    if (enemy.bounds.contains(p.x, p.y)) {
                        applyDamageToEnemy(enemy, p.damage, p.element)
                        spawnHitSparks(p.x, p.y, p.element.colorHex)
                        projIt.remove()
                        break
                    }
                }
            } else {
                // Enemy projectile hitting player
                if (player.bounds.contains(p.x, p.y)) {
                    applyDamageToPlayer(p.damage)
                    spawnHitSparks(p.x, p.y, 0xFFFF0055.toInt())
                    projIt.remove()
                }
            }
        }

        // Update Particles
        val partIt = particles.iterator()
        while (partIt.hasNext()) {
            val part = partIt.next()
            part.x += part.vx * dt
            part.y += part.vy * dt
            part.vy += 220f * dt // gravity
            part.life -= dt
            if (part.life <= 0f) partIt.remove()
        }

        // Update Floating Texts
        val textIt = floatingTexts.iterator()
        while (textIt.hasNext()) {
            val ft = textIt.next()
            ft.y += ft.vy * dt * 40f
            ft.life -= dt
            if (ft.life <= 0f) textIt.remove()
        }

        // Check Player Death
        if (player.currentHp <= 0) {
            isDefeat = true
            GameAudio.playDefeat()
        }
    }

    fun movePlayer(dir: Float, dt: Float, arenaWidth: Float) {
        if (isVictory || isDefeat) return
        val newX = player.x + dir * player.speed * dt * 75f
        player.x = newX.coerceIn(80f, arenaWidth - 80f)
    }

    fun performPlayerPunch() {
        if (punchCooldown > 0f || isVictory || isDefeat) return
        punchCooldown = 0.38f
        GameAudio.playPunchHit()

        val punchBox = RectF(player.x, player.y - 120f, player.x + 130f, player.y - 20f)
        var hitAny = false

        for (enemy in enemies) {
            if (RectF.intersects(punchBox, enemy.bounds)) {
                val isCrit = Math.random() < 0.22
                val dmg = if (isCrit) (player.attack * 1.6f).toInt() else player.attack
                applyDamageToEnemy(enemy, dmg, player.element, isCrit)
                enemy.x += 45f // knockback
                hitAny = true
            }
        }

        if (hitAny) {
            screenShakeAmount = 6f
            overdriveEnergy = (overdriveEnergy + 6f).coerceAtMost(100f)
        }
    }

    fun performCoreSkill() {
        if (coreSkillCooldown > 0f || isVictory || isDefeat) return
        coreSkillCooldown = 4.0f
        GameAudio.playCoreBlast()
        screenShakeAmount = 12f

        val core = GolemDatabase.getPart(playerConfig.coreId)
        when (core.element) {
            GolemElement.FIRE -> {
                // Erupt 3 flaming fireballs forward
                projectiles.add(Projectile(player.x + 40f, player.y - 70f, 420f, (player.attack * 1.5f).toInt(), true, GolemElement.FIRE, radius = 18f))
                projectiles.add(Projectile(player.x + 40f, player.y - 95f, 380f, (player.attack * 1.5f).toInt(), true, GolemElement.FIRE, radius = 16f))
                projectiles.add(Projectile(player.x + 40f, player.y - 45f, 450f, (player.attack * 1.5f).toInt(), true, GolemElement.FIRE, radius = 16f))
            }
            GolemElement.ICE -> {
                // Frost Nova: damages and chills all enemies
                for (enemy in enemies) {
                    applyDamageToEnemy(enemy, (player.attack * 1.4f).toInt(), GolemElement.ICE)
                    enemy.speed *= 0.6f // slow
                    spawnHitSparks(enemy.x, enemy.y - 40f, 0xFF00B4D8.toInt())
                }
            }
            GolemElement.ARCANE -> {
                // High-speed piercing arcane ray
                projectiles.add(Projectile(player.x + 40f, player.y - 70f, 750f, (player.attack * 2.2f).toInt(), true, GolemElement.ARCANE, radius = 22f, life = 3.5f))
            }
            else -> {
                // Earth/Stone Seismic Shockwave
                for (enemy in enemies) {
                    if (enemy.x - player.x in 0f..380f) {
                        applyDamageToEnemy(enemy, (player.attack * 1.3f).toInt(), GolemElement.STONE)
                        enemy.x += 70f // massive knockback
                    }
                }
                spawnHitSparks(player.x + 120f, player.y - 20f, 0xFFC27D38.toInt())
            }
        }
    }

    fun setPlayerBlocking(blocking: Boolean) {
        player.isBlocking = blocking
        if (blocking) {
            GameAudio.playShieldBlock()
        }
    }

    fun performOverdrive() {
        if (overdriveEnergy < 99f || isVictory || isDefeat) return
        overdriveEnergy = 0f
        screenShakeAmount = 25f
        GameAudio.playOverdrive()

        // Screen-clearing elemental apocalypse
        for (enemy in enemies) {
            val ultimateDmg = player.attack * 3 + 120
            applyDamageToEnemy(enemy, ultimateDmg, player.element, isCrit = true)
            enemy.x += 120f
            spawnDeathExplosion(enemy.x, enemy.y - 50f, player.element.colorHex)
        }
        floatingTexts.add(FloatingText("★ OVERDRIVE RAMPAGE! ★", player.x, player.y - 180f, -2.5f, 0xFFFFD166.toInt()))
    }

    private fun applyDamageToEnemy(enemy: CombatEntity, rawDmg: Int, element: GolemElement? = null, isCrit: Boolean = false) {
        val reduced = (rawDmg - enemy.armor * 0.35f).toInt().coerceAtLeast(5)
        enemy.currentHp -= reduced
        enemy.hitFlashTimer = 0.15f
        val color = if (isCrit) 0xFFFFD166.toInt() else (element?.colorHex ?: 0xFFFFFFFF.toInt())
        val text = if (isCrit) "CRIT $reduced!" else "$reduced"
        floatingTexts.add(FloatingText(text, enemy.x + (Math.random() * 20 - 10).toFloat(), enemy.y - enemy.height - 10f, -1.8f, color))
    }

    private fun applyDamageToPlayer(rawDmg: Int) {
        var dmg = rawDmg
        if (player.isBlocking) {
            dmg = (dmg * 0.25f).toInt().coerceAtLeast(1)
            floatingTexts.add(FloatingText("BLOCKED!", player.x, player.y - 150f, -1.5f, 0xFF00F5D4.toInt()))
            GameAudio.playShieldBlock()
        } else {
            dmg = (dmg - player.armor * 0.4f).toInt().coerceAtLeast(3)
            GameAudio.playPunchHit()
            screenShakeAmount = 8f
        }
        player.currentHp = (player.currentHp - dmg).coerceAtLeast(0)
        player.hitFlashTimer = 0.15f
        floatingTexts.add(FloatingText("-$dmg", player.x, player.y - 130f, -1.6f, 0xFFEF476F.toInt()))
    }

    private fun performEnemyAttack(enemy: CombatEntity) {
        enemy.attackCooldown = if (enemy.isBoss) 2.2f else 1.5f
        if (enemy.element == GolemElement.ARCANE) {
            // Shoots dark energy orb
            projectiles.add(Projectile(enemy.x - 30f, enemy.y - enemy.height * 0.6f, -320f, enemy.attack, false, enemy.element, radius = 14f))
        } else {
            // Melee swing
            applyDamageToPlayer(enemy.attack)
        }
    }

    private fun spawnWave(arenaWidth: Float, groundY: Float) {
        currentWave++
        waveSpawnTimer = 3.5f

        val isBossWave = (currentWave == maxWaves && stage?.bossType != null)
        if (isBossWave) {
            val bossType = stage?.bossType!!
            enemies.add(
                CombatEntity(
                    id = "boss",
                    name = bossType.displayName,
                    isPlayer = false,
                    x = arenaWidth - 100f,
                    y = groundY,
                    maxHp = bossType.baseHp,
                    currentHp = bossType.baseHp,
                    attack = bossType.baseAttack,
                    armor = 30,
                    speed = bossType.speed,
                    width = 130f,
                    height = 210f,
                    colorHex = bossType.colorHex,
                    element = bossType.element,
                    isBoss = true
                )
            )
            floatingTexts.add(FloatingText("⚠ BOSS: ${bossType.displayName} ⚠", arenaWidth / 2f, groundY - 240f, -0.8f, 0xFFFF0055.toInt(), 2.0f))
            return
        }

        // Standard wave spawns
        val enemyCount = 2 + (currentWave * 0.8f).toInt()
        val types = listOf(
            EnemyType.GOBLIN_SCAVENGER,
            EnemyType.CLOCKWORK_DRONE,
            EnemyType.SHADOW_WISP,
            EnemyType.MAGMA_HOUND
        )

        for (i in 0 until enemyCount) {
            val type = types[(Math.random() * types.size).toInt()]
            val spawnX = arenaWidth + 60f + i * 110f
            enemies.add(
                CombatEntity(
                    id = "enemy_${currentWave}_$i",
                    name = type.displayName,
                    isPlayer = false,
                    x = spawnX,
                    y = groundY,
                    maxHp = type.baseHp + currentWave * 15,
                    currentHp = type.baseHp + currentWave * 15,
                    attack = type.baseAttack + currentWave * 2,
                    armor = currentWave * 2,
                    speed = type.speed,
                    width = 75f,
                    height = 110f,
                    colorHex = type.colorHex,
                    element = type.element,
                    isBoss = false
                )
            )
        }
    }

    private fun spawnHitSparks(x: Float, y: Float, color: Int) {
        for (i in 0 until 10) {
            val angle = (Math.random() * 2 * Math.PI).toFloat()
            val spd = (50f + Math.random() * 150f).toFloat()
            particles.add(
                CombatParticle(
                    x = x, y = y,
                    vx = cos(angle) * spd,
                    vy = sin(angle) * spd,
                    color = color,
                    size = (2f + Math.random() * 3f).toFloat(),
                    life = 0.3f,
                    maxLife = 0.3f
                )
            )
        }
    }

    private fun spawnDeathExplosion(x: Float, y: Float, color: Int) {
        for (i in 0 until 24) {
            val angle = (Math.random() * 2 * Math.PI).toFloat()
            val spd = (60f + Math.random() * 260f).toFloat()
            particles.add(
                CombatParticle(
                    x = x, y = y,
                    vx = cos(angle) * spd,
                    vy = sin(angle) * spd - 60f,
                    color = color,
                    size = (4f + Math.random() * 5f).toFloat(),
                    life = 0.6f,
                    maxLife = 0.6f
                )
            )
        }
    }
}
