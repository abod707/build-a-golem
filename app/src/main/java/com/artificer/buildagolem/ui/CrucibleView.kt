package com.artificer.buildagolem.ui

import android.content.Context
import android.graphics.*
import android.os.SystemClock
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.artificer.buildagolem.audio.GameAudio
import kotlin.math.cos
import kotlin.math.sin

class CrucibleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var heatLevel: Float = 0.45f // 0.0 to 1.0
    var hammerStrikesRemaining: Int = 5
    var totalScore: Int = 0
    var isForgingActive: Boolean = false

    var onForgeCompletedListener: ((success: Boolean, qualityMultiplier: Float) -> Unit)? = null

    // Spark particles
    private data class Spark(
        var x: Float, var y: Float,
        var vx: Float, var vy: Float,
        var life: Float, var maxLife: Float,
        var color: Int
    )
    private val sparks = mutableListOf<Spark>()

    private val bgPaint = Paint().apply {
        shader = RadialGradient(
            500f, 400f, 500f,
            0xFF2C1612.toInt(),
            0xFF120B0A.toInt(),
            Shader.TileMode.CLAMP
        )
    }

    private val furnaceGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val anvilPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = 0xFF4A4E59.toInt()
    }
    private val anvilOutline = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = 0xFF23252B.toInt()
    }

    private val ingotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        textSize = 38f
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }

    private val subTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFA09DB5.toInt()
        textSize = 28f
        textAlign = Paint.Align.CENTER
    }

    private val gaugePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 16f
        strokeCap = Paint.Cap.ROUND
    }

    private var lastUpdateTime = SystemClock.uptimeMillis()

    fun startForgingSession() {
        heatLevel = 0.50f
        hammerStrikesRemaining = 5
        totalScore = 0
        isForgingActive = true
        invalidate()
    }

    fun pumpBellows() {
        if (!isForgingActive) return
        heatLevel = (heatLevel + 0.15f).coerceAtMost(1.0f)
        GameAudio.playCoreBlast()
        // Emit heat embers
        val w = width.toFloat()
        val h = height.toFloat()
        val anvilY = h * 0.58f
        for (i in 0 until 12) {
            val angle = (Math.random() * Math.PI).toFloat()
            val speed = (4f + Math.random() * 8f).toFloat()
            sparks.add(
                Spark(
                    x = w / 2f + (Math.random() * 60 - 30).toFloat(),
                    y = anvilY - 20f,
                    vx = cos(angle) * speed,
                    vy = -sin(angle) * speed,
                    life = 1.0f,
                    maxLife = (0.5f + Math.random() * 0.5f).toFloat(),
                    color = 0xFFFF5722.toInt()
                )
            )
        }
        invalidate()
    }

    fun strikeAnvil(): Int {
        if (!isForgingActive) return 0
        GameAudio.playHammerAnvil()

        // Calculate score based on heat: optimal zone is 0.50 - 0.75
        val hitScore = when {
            heatLevel in 0.52f..0.72f -> {
                totalScore += 100 // Perfect Masterwork strike
                100
            }
            heatLevel in 0.38f..0.85f -> {
                totalScore += 65 // Good strike
                65
            }
            else -> {
                totalScore += 25 // Too cold or too hot
                25
            }
        }

        // Spawn anvil impact sparks
        val w = width.toFloat()
        val h = height.toFloat()
        val anvilY = h * 0.58f
        for (i in 0 until 24) {
            val angle = (Math.random() * 2 * Math.PI).toFloat()
            val speed = (6f + Math.random() * 14f).toFloat()
            sparks.add(
                Spark(
                    x = w / 2f + (Math.random() * 40 - 20).toFloat(),
                    y = anvilY - 25f,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed * 0.7f - 3f,
                    life = 1.0f,
                    maxLife = (0.4f + Math.random() * 0.4f).toFloat(),
                    color = if (hitScore == 100) 0xFFFFD166.toInt() else 0xFFFF7043.toInt()
                )
            )
        }

        hammerStrikesRemaining--
        heatLevel = (heatLevel - 0.08f).coerceAtLeast(0.1f)

        if (hammerStrikesRemaining <= 0) {
            isForgingActive = false
            val multiplier = when {
                totalScore >= 450 -> 2.0f
                totalScore >= 350 -> 1.5f
                totalScore >= 250 -> 1.2f
                else -> 1.0f
            }
            postDelayed({
                onForgeCompletedListener?.invoke(true, multiplier)
            }, 600)
        }

        invalidate()
        return hitScore
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return

        val now = SystemClock.uptimeMillis()
        val dt = (now - lastUpdateTime) / 1000f
        lastUpdateTime = now

        // Heat cooling physics
        if (isForgingActive) {
            heatLevel = (heatLevel - dt * 0.07f).coerceAtLeast(0.1f)
        }

        // 1. Forge Background
        canvas.drawRect(0f, 0f, w, h, bgPaint)

        // 2. Furnace Heart Glow
        val centerX = w / 2f
        val anvilY = h * 0.58f
        furnaceGlowPaint.color = Color.argb(
            (60 + heatLevel * 140).toInt().coerceIn(0, 255),
            255,
            (heatLevel * 120).toInt(),
            20
        )
        canvas.drawCircle(centerX, anvilY - 30f, 180f * heatLevel, furnaceGlowPaint)

        // 3. Draw The Heavy Blacksmith Anvil
        val anvilPath = Path().apply {
            moveTo(centerX - 80f, anvilY - 20f) // Left horn
            lineTo(centerX + 80f, anvilY - 20f) // Right flat face
            lineTo(centerX + 70f, anvilY + 15f)
            lineTo(centerX + 35f, anvilY + 30f)
            lineTo(centerX + 60f, anvilY + 80f) // Base right
            lineTo(centerX - 60f, anvilY + 80f) // Base left
            lineTo(centerX - 35f, anvilY + 30f)
            lineTo(centerX - 70f, anvilY + 15f)
            close()
        }
        canvas.drawPath(anvilPath, anvilPaint)
        canvas.drawPath(anvilPath, anvilOutline)

        // 4. Hot Glowing Metal Ingot on top of Anvil
        val ingotRect = RectF(centerX - 45f, anvilY - 35f, centerX + 45f, anvilY - 18f)
        val ingotRed = 255
        val ingotGreen = (heatLevel * 200).toInt().coerceIn(40, 240)
        val ingotBlue = (heatLevel * 80).toInt().coerceIn(0, 100)
        ingotPaint.color = Color.rgb(ingotRed, ingotGreen, ingotBlue)
        canvas.drawRoundRect(ingotRect, 6f, 6f, ingotPaint)
        canvas.drawRoundRect(ingotRect, 6f, 6f, anvilOutline)

        // 5. Temperature Gauge Bar
        val gaugeY = 70f
        val gaugeWidth = w * 0.7f
        val gaugeLeft = (w - gaugeWidth) / 2f
        val gaugeRight = gaugeLeft + gaugeWidth

        // Cold Zone (Blue/Grey)
        gaugePaint.color = 0xFF546E7A.toInt()
        canvas.drawLine(gaugeLeft, gaugeY, gaugeLeft + gaugeWidth * 0.45f, gaugeY, gaugePaint)

        // Optimal Masterwork Zone (Green/Gold)
        gaugePaint.color = 0xFF06D6A0.toInt()
        canvas.drawLine(gaugeLeft + gaugeWidth * 0.45f, gaugeY, gaugeLeft + gaugeWidth * 0.75f, gaugeY, gaugePaint)

        // Overheat Zone (Crimson)
        gaugePaint.color = 0xFFEF476F.toInt()
        canvas.drawLine(gaugeLeft + gaugeWidth * 0.75f, gaugeY, gaugeRight, gaugeY, gaugePaint)

        // Gauge needle indicator
        val needleX = gaugeLeft + gaugeWidth * heatLevel
        canvas.drawCircle(needleX, gaugeY, 14f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            style = Paint.Style.FILL
        })
        canvas.drawCircle(needleX, gaugeY, 14f, anvilOutline)

        // 6. Sparks update & draw
        val it = sparks.iterator()
        while (it.hasNext()) {
            val spark = it.next()
            spark.x += spark.vx
            spark.y += spark.vy
            spark.vy += 0.4f // gravity
            spark.life -= dt / spark.maxLife
            if (spark.life <= 0f) {
                it.remove()
            } else {
                val sparkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = spark.color
                    alpha = (spark.life * 255).toInt().coerceIn(0, 255)
                }
                canvas.drawCircle(spark.x, spark.y, 3.5f * spark.life, sparkPaint)
            }
        }

        // 7. Status HUD
        if (isForgingActive) {
            textPaint.color = 0xFFFFD166.toInt()
            canvas.drawText("Strikes Left: $hammerStrikesRemaining   |   Score: $totalScore", centerX, gaugeY + 55f, textPaint)
            subTextPaint.color = if (heatLevel in 0.52f..0.72f) 0xFF06D6A0.toInt() else 0xFFA09DB5.toInt()
            val heatStatus = when {
                heatLevel < 0.45f -> "Too Cold! Pump Bellows to Heat Up"
                heatLevel > 0.78f -> "Too Hot! Let Ingot Cool Slightly"
                else -> "★ PERFECT HEAT! STRIKE THE ANVIL NOW! ★"
            }
            canvas.drawText(heatStatus, centerX, gaugeY + 95f, subTextPaint)
        } else {
            textPaint.color = 0xFFFFFFFF.toInt()
            canvas.drawText("The Master Crucible", centerX, gaugeY + 55f, textPaint)
            canvas.drawText("Tap 'Start Forging' below to temper metals and craft parts", centerX, gaugeY + 95f, subTextPaint)
        }

        postInvalidateOnAnimation()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val h = height.toFloat()
            val anvilY = h * 0.58f
            // If user taps directly on anvil area, strike anvil
            if (isForgingActive && event.y in (anvilY - 60f)..(anvilY + 90f)) {
                strikeAnvil()
                performClick()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }
}
