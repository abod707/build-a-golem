package com.artificer.buildagolem.ui

import android.content.Context
import android.graphics.*
import android.os.SystemClock
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.artificer.buildagolem.model.GolemConfiguration
import com.artificer.buildagolem.model.PartSlot
import com.artificer.buildagolem.renderer.GolemPose
import com.artificer.buildagolem.renderer.GolemRenderer
import kotlin.math.cos
import kotlin.math.sin

class WorkshopView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val renderer = GolemRenderer()
    var currentConfig: GolemConfiguration = GolemConfiguration()
        set(value) {
            field = value
            invalidate()
        }

    var currentPose: GolemPose = GolemPose.IDLE
    private var poseResetTimeMs: Long = 0
    private var startTimeMs: Long = SystemClock.uptimeMillis()

    var onSlotSelectedListener: ((PartSlot) -> Unit)? = null

    // Background and ambient particles
    private val bgPaint = Paint().apply {
        shader = RadialGradient(
            500f, 500f, 600f,
            0xFF231F34.toInt(),
            0xFF12101B.toInt(),
            Shader.TileMode.CLAMP
        )
    }
    private val pedestalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = 0xFF2A263D.toInt()
    }
    private val pedestalRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = 0xFF534C73.toInt()
    }
    private val runeGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = 0x88FFD166.toInt()
    }

    private data class AmbientMote(var x: Float, var y: Float, var speedY: Float, var radius: Float, var alpha: Int)
    private val motes = Array(25) {
        AmbientMote(
            x = (Math.random() * 1000).toFloat(),
            y = (Math.random() * 1000).toFloat(),
            speedY = (0.5f + Math.random() * 1.5f).toFloat(),
            radius = (1.5f + Math.random() * 3f).toFloat(),
            alpha = (60 + Math.random() * 120).toInt()
        )
    }
    private val motePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = 0xFFE0AAFF.toInt()
    }

    fun triggerPose(pose: GolemPose, durationMs: Long = 1200) {
        currentPose = pose
        poseResetTimeMs = SystemClock.uptimeMillis() + durationMs
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return

        val now = SystemClock.uptimeMillis()
        val elapsedSec = (now - startTimeMs) / 1000f

        // Check pose expiration
        if (currentPose != GolemPose.IDLE && now >= poseResetTimeMs) {
            currentPose = GolemPose.IDLE
        }

        // Draw Dark Alchemical Workshop Background
        canvas.drawRect(0f, 0f, w, h, bgPaint)

        // Draw Ambient floating motes
        for (mote in motes) {
            mote.y -= mote.speedY
            if (mote.y < 0) {
                mote.y = h
                mote.x = (Math.random() * w).toFloat()
            }
            motePaint.alpha = mote.alpha
            canvas.drawCircle(mote.x, mote.y, mote.radius, motePaint)
        }

        // Workshop Stone Pedestal & Rune Circles
        val centerX = w / 2f
        val centerY = h * 0.52f
        val pedestalY = centerY + 90f

        val pedestalOval = RectF(centerX - 130f, pedestalY - 25f, centerX + 130f, pedestalY + 25f)
        canvas.drawOval(pedestalOval, pedestalPaint)
        canvas.drawOval(pedestalOval, pedestalRingPaint)

        // Pulsing runic circle on pedestal
        val innerOval = RectF(centerX - 95f, pedestalY - 18f, centerX + 95f, pedestalY + 18f)
        runeGlowPaint.alpha = (100 + sin(elapsedSec * 4f) * 60).toInt().coerceIn(0, 255)
        canvas.drawOval(innerOval, runeGlowPaint)

        // Render Assembled Golem
        val scale = (w / 380f).coerceIn(1.0f, 1.6f)
        renderer.drawGolem(
            canvas = canvas,
            config = currentConfig,
            centerX = centerX,
            centerY = centerY,
            scale = scale,
            animTimeSec = elapsedSec,
            pose = currentPose,
            facingRight = true
        )

        // Continuously animate
        postInvalidateOnAnimation()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val w = width.toFloat()
            val h = height.toFloat()
            val centerX = w / 2f
            val centerY = h * 0.52f
            val dx = event.x - centerX
            val dy = event.y - centerY

            // Determine touched anatomical slot based on tap location relative to golem center
            val slot = when {
                dy in -130f..-45f && kotlin.math.abs(dx) < 70f -> PartSlot.HEAD
                dy in -45f..30f && kotlin.math.abs(dx) < 35f -> PartSlot.CORE
                dy in -45f..35f && kotlin.math.abs(dx) < 70f -> PartSlot.TORSO
                dy in -30f..80f && kotlin.math.abs(dx) in 40f..120f -> PartSlot.ARMS
                dy in 35f..120f && kotlin.math.abs(dx) < 80f -> PartSlot.LEGS
                else -> null
            }

            if (slot != null) {
                onSlotSelectedListener?.invoke(slot)
                triggerPose(GolemPose.ATTACK_PUNCH, 600)
                performClick()
                return true
            } else {
                // Background tap: trigger friendly roar / emote
                triggerPose(GolemPose.IDLE, 500)
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }
}
