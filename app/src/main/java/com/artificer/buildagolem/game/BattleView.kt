package com.artificer.buildagolem.game

import android.content.Context
import android.graphics.*
import android.os.SystemClock
import android.util.AttributeSet
import android.view.View
import com.artificer.buildagolem.model.GolemConfiguration
import com.artificer.buildagolem.model.GolemDatabase
import com.artificer.buildagolem.model.GolemElement
import com.artificer.buildagolem.renderer.GolemPose
import com.artificer.buildagolem.renderer.GolemRenderer
import kotlin.math.sin

class BattleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var engine: BattleEngine? = null
        set(value) {
            field = value
            lastFrameTime = SystemClock.uptimeMillis()
            invalidate()
        }

    private val golemRenderer = GolemRenderer()
    private var lastFrameTime = SystemClock.uptimeMillis()

    // Paints
    private val skyPaint = Paint().apply {
        shader = LinearGradient(0f, 0f, 0f, 500f, 0xFF161224.toInt(), 0xFF2A1B3D.toInt(), Shader.TileMode.CLAMP)
    }
    private val groundPaint = Paint().apply {
        color = 0xFF1B1924.toInt()
        style = Paint.Style.FILL
    }
    private val groundLinePaint = Paint().apply {
        color = 0xFF36324A.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }
    private val hpBarBgPaint = Paint().apply {
        color = 0xFF333344.toInt()
        style = Paint.Style.FILL
    }
    private val playerHpPaint = Paint().apply {
        color = 0xFF06D6A0.toInt()
        style = Paint.Style.FILL
    }
    private val enemyHpPaint = Paint().apply {
        color = 0xFFEF476F.toInt()
        style = Paint.Style.FILL
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        textSize = 32f
        typeface = Typeface.DEFAULT_BOLD
    }
    private val centerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        textSize = 44f
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }
    private val particlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val flashPaint = Paint().apply {
        color = 0xAAFFFFFF.toInt()
        style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val eng = engine ?: return

        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return

        val now = SystemClock.uptimeMillis()
        val dt = ((now - lastFrameTime) / 1000f).coerceIn(0.001f, 0.05f)
        lastFrameTime = now

        val groundY = h * 0.72f

        // Update physics
        eng.update(dt, w, groundY)

        // Screen Shake
        canvas.save()
        if (eng.screenShakeAmount > 0.5f) {
            val shakeX = (Math.random() * eng.screenShakeAmount * 2 - eng.screenShakeAmount).toFloat()
            val shakeY = (Math.random() * eng.screenShakeAmount * 2 - eng.screenShakeAmount).toFloat()
            canvas.translate(shakeX, shakeY)
        }

        // 1. Arena Sky & Backdrop
        canvas.drawRect(0f, 0f, w, groundY, skyPaint)

        // Distant Mountain Silhouette
        val mountainPath = Path().apply {
            moveTo(0f, groundY)
            lineTo(w * 0.2f, groundY - 140f)
            lineTo(w * 0.45f, groundY - 60f)
            lineTo(w * 0.75f, groundY - 180f)
            lineTo(w, groundY - 90f)
            lineTo(w, groundY)
            close()
        }
        canvas.drawPath(mountainPath, Paint().apply { color = 0xFF211B30.toInt() })

        // 2. Ground Plane
        canvas.drawRect(0f, groundY, w, h, groundPaint)
        canvas.drawLine(0f, groundY, w, groundY, groundLinePaint)

        // 3. Render Enemies
        for (enemy in eng.enemies) {
            drawEnemy(canvas, enemy)
        }

        // 4. Render Player Golem
        val playerPose = when {
            eng.player.isBlocking -> GolemPose.DEFEND_BLOCK
            eng.punchCooldown > 0.15f -> GolemPose.ATTACK_PUNCH
            eng.overdriveEnergy >= 100f -> GolemPose.OVERDRIVE_ROAR
            else -> GolemPose.IDLE
        }

        golemRenderer.drawGolem(
            canvas = canvas,
            config = eng.playerConfig,
            centerX = eng.player.x,
            centerY = groundY - 80f,
            scale = 0.85f,
            animTimeSec = eng.player.stateTime,
            pose = playerPose,
            facingRight = true
        )

        // Hit flash overlay for player
        if (eng.player.hitFlashTimer > 0f) {
            canvas.drawRect(eng.player.bounds, flashPaint)
        }

        // 5. Render Projectiles
        for (proj in eng.projectiles) {
            val pPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = proj.element.colorHex
                style = Paint.Style.FILL
            }
            canvas.drawCircle(proj.x, proj.y, proj.radius, pPaint)
            // Hot white core
            canvas.drawCircle(proj.x, proj.y, proj.radius * 0.5f, Paint().apply { color = 0xFFFFFFFF.toInt() })
        }

        // 6. Render Particles
        for (part in eng.particles) {
            particlePaint.color = part.color
            particlePaint.alpha = ((part.life / part.maxLife) * 255).toInt().coerceIn(0, 255)
            canvas.drawCircle(part.x, part.y, part.size, particlePaint)
        }

        // 7. Render Floating Text / Damage Numbers
        for (ft in eng.floatingTexts) {
            val ftPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = ft.color
                textSize = 34f
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.CENTER
                alpha = (ft.life * 255).toInt().coerceIn(0, 255)
            }
            canvas.drawText(ft.text, ft.x, ft.y, ftPaint)
        }

        canvas.restore() // End Screen Shake

        // 8. Player HUD (Pinned at Top)
        drawPlayerHUD(canvas, eng, w)

        // 9. Victory or Defeat Overlay
        if (eng.isVictory) {
            drawEndScreen(canvas, w, h, isVictory = true, eng)
        } else if (eng.isDefeat) {
            drawEndScreen(canvas, w, h, isVictory = false, eng)
        }

        postInvalidateOnAnimation()
    }

    private fun drawEnemy(canvas: Canvas, enemy: CombatEntity) {
        val b = enemy.bounds
        val enemyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (enemy.hitFlashTimer > 0f) 0xFFFFFFFF.toInt() else enemy.colorHex
            style = Paint.Style.FILL
        }

        if (enemy.isBoss) {
            // Colossal Boss Monster Silhouette
            canvas.drawRoundRect(b, 20f, 20f, enemyPaint)
            // Glowing Boss Eyes
            val eyePaint = Paint().apply { color = 0xFFFFD166.toInt() }
            canvas.drawCircle(enemy.x - 25f, enemy.y - enemy.height + 40f, 8f, eyePaint)
            canvas.drawCircle(enemy.x + 25f, enemy.y - enemy.height + 40f, 8f, eyePaint)
        } else {
            // Minion Monster
            canvas.drawRoundRect(b, 12f, 12f, enemyPaint)
            // Imp / Drone Eyes
            val eyePaint = Paint().apply { color = 0xFFFF0055.toInt() }
            canvas.drawCircle(enemy.x - 12f, enemy.y - enemy.height + 25f, 4f, eyePaint)
            canvas.drawCircle(enemy.x + 12f, enemy.y - enemy.height + 25f, 4f, eyePaint)
        }

        // Health Bar Above Head
        val barWidth = enemy.width * 1.1f
        val barHeight = if (enemy.isBoss) 12f else 7f
        val barLeft = enemy.x - barWidth / 2f
        val barTop = enemy.y - enemy.height - 18f
        val hpPct = (enemy.currentHp.toFloat() / enemy.maxHp).coerceIn(0f, 1f)

        canvas.drawRoundRect(RectF(barLeft, barTop, barLeft + barWidth, barTop + barHeight), 4f, 4f, hpBarBgPaint)
        canvas.drawRoundRect(RectF(barLeft, barTop, barLeft + barWidth * hpPct, barTop + barHeight), 4f, 4f, enemyHpPaint)
    }

    private fun drawPlayerHUD(canvas: Canvas, eng: BattleEngine, w: Float) {
        // Player HP Bar
        val barLeft = 30f
        val barTop = 35f
        val barWidth = w * 0.45f
        val barHeight = 22f
        val hpPct = (eng.player.currentHp.toFloat() / eng.player.maxHp).coerceIn(0f, 1f)

        canvas.drawRoundRect(RectF(barLeft, barTop, barLeft + barWidth, barTop + barHeight), 6f, 6f, hpBarBgPaint)
        canvas.drawRoundRect(RectF(barLeft, barTop, barLeft + barWidth * hpPct, barTop + barHeight), 6f, 6f, playerHpPaint)

        textPaint.textSize = 24f
        textPaint.color = 0xFFFFFFFF.toInt()
        canvas.drawText("${eng.player.name}: ${eng.player.currentHp} / ${eng.player.maxHp} HP", barLeft, barTop + barHeight + 28f, textPaint)

        // Wave counter
        val waveText = if (eng.isEndless) "Score: ${eng.score}" else "Wave ${eng.currentWave} / ${eng.maxWaves}"
        val waveWidth = textPaint.measureText(waveText)
        canvas.drawText(waveText, w - waveWidth - 30f, barTop + 24f, textPaint)

        // Overdrive Energy Meter
        val odLeft = barLeft
        val odTop = barTop + barHeight + 36f
        val odWidth = barWidth * 0.7f
        val odHeight = 12f
        val odPct = (eng.overdriveEnergy / 100f).coerceIn(0f, 1f)

        canvas.drawRoundRect(RectF(odLeft, odTop, odLeft + odWidth, odTop + odHeight), 4f, 4f, hpBarBgPaint)
        val odPaint = Paint().apply {
            color = if (eng.overdriveEnergy >= 99f) 0xFFFFD166.toInt() else 0xFF118AB2.toInt()
        }
        canvas.drawRoundRect(RectF(odLeft, odTop, odLeft + odWidth * odPct, odTop + odHeight), 4f, 4f, odPaint)
    }

    private fun drawEndScreen(canvas: Canvas, w: Float, h: Float, isVictory: Boolean, eng: BattleEngine) {
        // Dark translucent shroud
        canvas.drawRect(0f, 0f, w, h, Paint().apply { color = 0xDD0C0B12.toInt() })

        val centerY = h * 0.45f
        if (isVictory) {
            centerTextPaint.color = 0xFFFFD166.toInt()
            canvas.drawText("🏆 VICTORY! 🏆", w / 2f, centerY - 60f, centerTextPaint)
            textPaint.color = 0xFF06D6A0.toInt()
            textPaint.textSize = 32f
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("Sanctum Defended! Spoils Claimed:", w / 2f, centerY - 10f, textPaint)
            textPaint.color = 0xFFFFFFFF.toInt()
            canvas.drawText("+${eng.collectedGold} Gold  |  +${eng.collectedClay} Clay  |  +${eng.collectedOre} Ore  |  +${eng.collectedCrystals} Crystal", w / 2f, centerY + 45f, textPaint)
            textPaint.textAlign = Paint.Align.LEFT
        } else {
            centerTextPaint.color = 0xFFEF476F.toInt()
            canvas.drawText("💀 GOLEM CRUMBLED 💀", w / 2f, centerY - 60f, centerTextPaint)
            textPaint.color = 0xFFA09DB5.toInt()
            textPaint.textSize = 30f
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("Reinforce your golem in the Workshop and try again!", w / 2f, centerY, textPaint)
            textPaint.textAlign = Paint.Align.LEFT
        }
    }
}
