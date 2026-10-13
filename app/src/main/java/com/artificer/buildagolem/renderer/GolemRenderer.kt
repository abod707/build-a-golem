package com.artificer.buildagolem.renderer

import android.graphics.*
import com.artificer.buildagolem.model.GolemConfiguration
import com.artificer.buildagolem.model.GolemDatabase
import com.artificer.buildagolem.model.GolemElement
import com.artificer.buildagolem.model.PartSlot
import kotlin.math.*

enum class GolemPose {
    IDLE,
    WALK,
    ATTACK_PUNCH,
    DEFEND_BLOCK,
    OVERDRIVE_ROAR
}

class GolemRenderer {

    private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = 0xFF1A1A24.toInt()
    }
    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = 0x33FFFFFF
    }
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = 0x55000000
    }
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val runePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        strokeCap = Paint.Cap.ROUND
    }
    private val eyePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    /**
     * Render the assembled golem at (centerX, centerY) with given scale and animation phase.
     */
    fun drawGolem(
        canvas: Canvas,
        config: GolemConfiguration,
        centerX: Float,
        centerY: Float,
        scale: Float = 1.0f,
        animTimeSec: Float = 0f,
        pose: GolemPose = GolemPose.IDLE,
        facingRight: Boolean = true
    ) {
        val core = GolemDatabase.getPart(config.coreId)
        val head = GolemDatabase.getPart(config.headId)
        val torso = GolemDatabase.getPart(config.torsoId)
        val arms = GolemDatabase.getPart(config.armsId)
        val legs = GolemDatabase.getPart(config.legsId)

        canvas.save()
        canvas.translate(centerX, centerY)
        val dirX = if (facingRight) 1f else -1f
        canvas.scale(scale * dirX, scale)

        // Animation calculations
        val idleBob = sin(animTimeSec * 3.0f) * 6f
        val breathScale = 1.0f + sin(animTimeSec * 2.5f) * 0.025f

        var leftLegAngle = 0f
        var rightLegAngle = 0f
        var leftArmAngle = 0f
        var rightArmAngle = 0f
        var punchExtension = 0f
        var torsoTilt = 0f

        when (pose) {
            GolemPose.IDLE -> {
                leftLegAngle = sin(animTimeSec * 1.5f) * 3f
                rightLegAngle = -sin(animTimeSec * 1.5f) * 3f
                leftArmAngle = sin(animTimeSec * 2.0f) * 6f
                rightArmAngle = -sin(animTimeSec * 2.0f) * 6f
            }
            GolemPose.WALK -> {
                leftLegAngle = sin(animTimeSec * 9f) * 22f
                rightLegAngle = -sin(animTimeSec * 9f) * 22f
                leftArmAngle = -sin(animTimeSec * 9f) * 25f
                rightArmAngle = sin(animTimeSec * 9f) * 25f
                torsoTilt = sin(animTimeSec * 9f) * 3f
            }
            GolemPose.ATTACK_PUNCH -> {
                val punchCycle = (animTimeSec * 12f) % (2f * PI.toFloat())
                val punchPhase = sin(punchCycle)
                rightArmAngle = -45f + punchPhase * 55f
                leftArmAngle = 20f
                punchExtension = (punchPhase.coerceAtLeast(0f)) * 40f
                torsoTilt = 8f
            }
            GolemPose.DEFEND_BLOCK -> {
                leftArmAngle = -65f
                rightArmAngle = -65f
                torsoTilt = -5f
            }
            GolemPose.OVERDRIVE_ROAR -> {
                leftArmAngle = -80f + sin(animTimeSec * 20f) * 10f
                rightArmAngle = -80f - sin(animTimeSec * 20f) * 10f
                torsoTilt = -8f
            }
        }

        // 1. Ground Shadow
        canvas.drawOval(
            RectF(-65f, 95f, 65f, 120f),
            shadowPaint
        )

        // 2. Legs (Back / Left Leg first, then Right Leg later or both here)
        drawLeg(canvas, legs.element, -26f, 35f, leftLegAngle)
        drawLeg(canvas, legs.element, 26f, 35f, rightLegAngle)

        // 3. Back Arm (Left Arm)
        canvas.save()
        canvas.translate(-42f, -10f + idleBob)
        canvas.rotate(leftArmAngle)
        drawArm(canvas, arms.element, isLeft = true, punchExtension = 0f)
        canvas.restore()

        // 4. Torso & Core
        canvas.save()
        canvas.translate(0f, idleBob)
        canvas.scale(breathScale, breathScale)
        canvas.rotate(torsoTilt)

        drawTorso(canvas, torso.element)
        drawCore(canvas, core.element, animTimeSec, isOverdrive = (pose == GolemPose.OVERDRIVE_ROAR))

        // 5. Head
        canvas.save()
        canvas.translate(0f, -55f)
        drawHead(canvas, head.element, animTimeSec)
        canvas.restore()

        canvas.restore() // End Torso context

        // 6. Front Arm (Right Arm)
        canvas.save()
        canvas.translate(42f, -10f + idleBob)
        canvas.rotate(rightArmAngle)
        drawArm(canvas, arms.element, isLeft = false, punchExtension = punchExtension)
        canvas.restore()

        // 7. Elemental Aura particles/overlay when in overdrive
        if (pose == GolemPose.OVERDRIVE_ROAR) {
            drawOverdriveAura(canvas, core.element, animTimeSec)
        }

        canvas.restore()
    }

    private fun drawLeg(canvas: Canvas, element: GolemElement, offsetX: Float, offsetY: Float, angle: Float) {
        canvas.save()
        canvas.translate(offsetX, offsetY)
        canvas.rotate(angle)

        bodyPaint.color = element.colorHex
        when (element) {
            GolemElement.EARTH, GolemElement.STONE -> {
                // Thick stone pillar
                val legRect = RectF(-16f, 0f, 16f, 65f)
                canvas.drawRoundRect(legRect, 10f, 10f, bodyPaint)
                canvas.drawRoundRect(legRect, 10f, 10f, outlinePaint)
                // Foot
                val footRect = RectF(-18f, 50f, 22f, 70f)
                canvas.drawRoundRect(footRect, 6f, 6f, bodyPaint)
                canvas.drawRoundRect(footRect, 6f, 6f, outlinePaint)
            }
            GolemElement.IRON -> {
                // Hydraulic piston
                val pistonOuter = RectF(-14f, 0f, 14f, 35f)
                bodyPaint.color = 0xFF5D6470.toInt()
                canvas.drawRect(pistonOuter, bodyPaint)
                canvas.drawRect(pistonOuter, outlinePaint)
                // Chrome piston rod
                bodyPaint.color = 0xFFE0E0E0.toInt()
                canvas.drawRect(RectF(-8f, 25f, 8f, 58f), bodyPaint)
                canvas.drawRect(RectF(-8f, 25f, 8f, 58f), outlinePaint)
                // Spiked foot
                bodyPaint.color = 0xFF424242.toInt()
                canvas.drawRoundRect(RectF(-20f, 52f, 20f, 68f), 4f, 4f, bodyPaint)
                canvas.drawRoundRect(RectF(-20f, 52f, 20f, 68f), 4f, 4f, outlinePaint)
            }
            GolemElement.FIRE -> {
                // Molten basalt leg with lava cracks
                val legRect = RectF(-15f, 0f, 15f, 62f)
                bodyPaint.color = 0xFF2B2523.toInt()
                canvas.drawRoundRect(legRect, 8f, 8f, bodyPaint)
                canvas.drawRoundRect(legRect, 8f, 8f, outlinePaint)
                // Lava fissure
                runePaint.color = 0xFFFF5722.toInt()
                canvas.drawLine(-6f, 10f, 4f, 28f, runePaint)
                canvas.drawLine(4f, 28f, -4f, 48f, runePaint)
                // Scorched foot
                canvas.drawRoundRect(RectF(-18f, 48f, 20f, 66f), 5f, 5f, bodyPaint)
                canvas.drawRoundRect(RectF(-18f, 48f, 20f, 66f), 5f, 5f, outlinePaint)
            }
            GolemElement.ARCANE -> {
                // Hover levitation rings instead of walking legs
                bodyPaint.color = 0xFF9D4EDD.toInt()
                val ring1 = RectF(-18f, 20f, 18f, 32f)
                val ring2 = RectF(-14f, 42f, 14f, 50f)
                canvas.drawOval(ring1, bodyPaint)
                canvas.drawOval(ring1, outlinePaint)
                glowPaint.color = 0x88C77DFF.toInt()
                canvas.drawOval(ring2, glowPaint)
                canvas.drawOval(ring2, outlinePaint)
            }
            else -> {
                val legRect = RectF(-15f, 0f, 15f, 64f)
                canvas.drawRoundRect(legRect, 8f, 8f, bodyPaint)
                canvas.drawRoundRect(legRect, 8f, 8f, outlinePaint)
            }
        }
        canvas.restore()
    }

    private fun drawTorso(canvas: Canvas, element: GolemElement) {
        val torsoPath = Path().apply {
            moveTo(-50f, -40f) // Left shoulder
            lineTo(50f, -40f)  // Right shoulder
            lineTo(38f, 38f)   // Waist right
            lineTo(-38f, 38f)  // Waist left
            close()
        }

        bodyPaint.color = element.colorHex
        canvas.drawPath(torsoPath, bodyPaint)
        canvas.drawPath(torsoPath, outlinePaint)

        // Material-specific torso details
        when (element) {
            GolemElement.EARTH -> {
                // Terracotta plate layers + moss specks
                highlightPaint.color = 0x44A06020.toInt()
                canvas.drawRect(RectF(-35f, -25f, 35f, 5f), highlightPaint)
                // Green moss specks
                runePaint.color = 0xFF4CAF50.toInt()
                canvas.drawCircle(-25f, -20f, 4f, runePaint)
                canvas.drawCircle(-15f, -28f, 5f, runePaint)
                canvas.drawCircle(30f, 10f, 4f, runePaint)
            }
            GolemElement.STONE -> {
                // Chiseled rock cracks
                runePaint.color = 0xFF2B2D42.toInt()
                canvas.drawLine(-35f, -20f, -15f, -5f, runePaint)
                canvas.drawLine(20f, -25f, 10f, 15f, runePaint)
                canvas.drawLine(10f, 15f, 25f, 30f, runePaint)
            }
            GolemElement.IRON -> {
                // Steel breastplate rivets
                eyePaint.color = 0xFFFFD166.toInt()
                canvas.drawCircle(-40f, -32f, 3.5f, eyePaint)
                canvas.drawCircle(40f, -32f, 3.5f, eyePaint)
                canvas.drawCircle(-30f, 30f, 3.5f, eyePaint)
                canvas.drawCircle(30f, 30f, 3.5f, eyePaint)
                // Center seam
                runePaint.color = 0xFF303540.toInt()
                canvas.drawLine(0f, -38f, 0f, 36f, runePaint)
            }
            GolemElement.FIRE -> {
                // Lava fissure glow on chest
                glowPaint.color = 0xAAFF3D00.toInt()
                canvas.drawCircle(0f, 0f, 32f, glowPaint)
                runePaint.color = 0xFFFFC107.toInt()
                canvas.drawLine(-25f, -20f, -5f, 10f, runePaint)
                canvas.drawLine(25f, -20f, 5f, 10f, runePaint)
            }
            GolemElement.ARCANE -> {
                // Sacred rune geometry
                runePaint.color = 0xFFE0AAFF.toInt()
                val diamond = Path().apply {
                    moveTo(0f, -30f)
                    lineTo(35f, 0f)
                    lineTo(0f, 30f)
                    lineTo(-35f, 0f)
                    close()
                }
                canvas.drawPath(diamond, runePaint)
            }
            else -> {}
        }
    }

    private fun drawCore(canvas: Canvas, element: GolemElement, animTime: Float, isOverdrive: Boolean) {
        val coreRadius = if (isOverdrive) 22f else 16f
        val pulse = 1.0f + sin(animTime * 6f) * 0.15f

        // Outer glow
        glowPaint.color = when (element) {
            GolemElement.FIRE -> 0x66FF5722.toInt()
            GolemElement.ICE -> 0x6600B4D8.toInt()
            GolemElement.LIGHTNING -> 0x66FFD166.toInt()
            GolemElement.ARCANE -> 0x667209B7.toInt()
            else -> 0x6606D6A0.toInt()
        }
        canvas.drawCircle(0f, 2f, coreRadius * 2.2f * pulse, glowPaint)

        // Core Housing (Dark metallic socket)
        eyePaint.color = 0xFF1C1924.toInt()
        canvas.drawCircle(0f, 2f, coreRadius + 4f, eyePaint)
        canvas.drawCircle(0f, 2f, coreRadius + 4f, outlinePaint)

        // The Gem/Core Heart
        bodyPaint.color = element.colorHex
        canvas.drawCircle(0f, 2f, coreRadius * pulse, bodyPaint)

        // Center Hotspot
        eyePaint.color = 0xFFFFFFFF.toInt()
        canvas.drawCircle(-3f, -1f, coreRadius * 0.45f, eyePaint)

        // Rotating mystic orbital glyphs
        val orbitAngle = (animTime * 90f) % 360f
        canvas.save()
        canvas.translate(0f, 2f)
        canvas.rotate(orbitAngle)
        runePaint.color = 0xAAFFFFFF.toInt()
        canvas.drawCircle(coreRadius + 7f, 0f, 2.5f, runePaint)
        canvas.drawCircle(-(coreRadius + 7f), 0f, 2.5f, runePaint)
        canvas.restore()
    }

    private fun drawHead(canvas: Canvas, element: GolemElement, animTime: Float) {
        val headRect = RectF(-26f, -32f, 26f, 10f)
        bodyPaint.color = element.colorHex
        canvas.drawRoundRect(headRect, 10f, 10f, bodyPaint)
        canvas.drawRoundRect(headRect, 10f, 10f, outlinePaint)

        // Head accessories / Features
        when (element) {
            GolemElement.EARTH -> {
                // Calm dual amber eyes
                eyePaint.color = 0xFFFFD166.toInt()
                canvas.drawCircle(-10f, -10f, 4f, eyePaint)
                canvas.drawCircle(10f, -10f, 4f, eyePaint)
            }
            GolemElement.STONE -> {
                // Heavy brow ridge & slit eyes
                runePaint.color = 0xFF2B2D42.toInt()
                canvas.drawLine(-20f, -16f, 20f, -16f, runePaint)
                eyePaint.color = 0xFF00F5D4.toInt()
                canvas.drawRect(RectF(-14f, -12f, -6f, -7f), eyePaint)
                canvas.drawRect(RectF(6f, -12f, 14f, -7f), eyePaint)
            }
            GolemElement.IRON -> {
                // Knight visor slit
                eyePaint.color = 0xFF000000.toInt()
                canvas.drawRect(RectF(-18f, -14f, 18f, -6f), eyePaint)
                // Red glowing scanning beam
                val scanX = sin(animTime * 4f) * 10f
                eyePaint.color = 0xFFFF0055.toInt()
                canvas.drawRect(RectF(scanX - 3f, -12f, scanX + 3f, -8f), eyePaint)
            }
            GolemElement.FIRE -> {
                // Infernal Ram Horns
                val leftHorn = Path().apply {
                    moveTo(-24f, -22f)
                    quadTo(-44f, -38f, -32f, -50f)
                    lineTo(-20f, -28f)
                    close()
                }
                val rightHorn = Path().apply {
                    moveTo(24f, -22f)
                    quadTo(44f, -38f, 32f, -50f)
                    lineTo(20f, -28f)
                    close()
                }
                bodyPaint.color = 0xFF3E2723.toInt()
                canvas.drawPath(leftHorn, bodyPaint)
                canvas.drawPath(leftHorn, outlinePaint)
                canvas.drawPath(rightHorn, bodyPaint)
                canvas.drawPath(rightHorn, outlinePaint)

                // Burning fire eyes
                eyePaint.color = 0xFFFFEB3B.toInt()
                canvas.drawCircle(-10f, -10f, 4.5f, eyePaint)
                canvas.drawCircle(10f, -10f, 4.5f, eyePaint)
            }
            GolemElement.ARCANE -> {
                // Cyclops Lens
                val lensRect = RectF(-12f, -18f, 12f, 2f)
                bodyPaint.color = 0xFF1C1924.toInt()
                canvas.drawOval(lensRect, bodyPaint)
                canvas.drawOval(lensRect, outlinePaint)
                eyePaint.color = 0xFFE0AAFF.toInt()
                canvas.drawCircle(0f, -8f, 7f, eyePaint)
                eyePaint.color = 0xFFFFFFFF.toInt()
                canvas.drawCircle(-2f, -10f, 2.5f, eyePaint)
            }
            GolemElement.LIGHTNING -> {
                // Storm Crown Spikes
                val crownPath = Path().apply {
                    moveTo(-26f, -32f)
                    lineTo(-18f, -48f)
                    lineTo(-8f, -32f)
                    lineTo(0f, -54f)
                    lineTo(8f, -32f)
                    lineTo(18f, -48f)
                    lineTo(26f, -32f)
                }
                bodyPaint.color = 0xFFFFD166.toInt()
                canvas.drawPath(crownPath, bodyPaint)
                canvas.drawPath(crownPath, outlinePaint)
                eyePaint.color = 0xFFFFFF00.toInt()
                canvas.drawCircle(-10f, -10f, 4f, eyePaint)
                canvas.drawCircle(10f, -10f, 4f, eyePaint)
            }
            else -> {
                eyePaint.color = 0xFF00B4D8.toInt()
                canvas.drawCircle(-10f, -10f, 4f, eyePaint)
                canvas.drawCircle(10f, -10f, 4f, eyePaint)
            }
        }
    }

    private fun drawArm(canvas: Canvas, element: GolemElement, isLeft: Boolean, punchExtension: Float) {
        // Shoulder pauldrons
        bodyPaint.color = element.colorHex
        canvas.drawCircle(0f, 0f, 18f, bodyPaint)
        canvas.drawCircle(0f, 0f, 18f, outlinePaint)

        // Forearm extending down/forward
        val armPath = Path().apply {
            moveTo(-10f, 10f)
            lineTo(10f, 10f)
            lineTo(14f + punchExtension * 0.4f, 45f + punchExtension)
            lineTo(-14f + punchExtension * 0.4f, 45f + punchExtension)
            close()
        }
        canvas.drawPath(armPath, bodyPaint)
        canvas.drawPath(armPath, outlinePaint)

        // Massive Fist / Weapon Hand
        canvas.save()
        canvas.translate(punchExtension * 0.4f, 48f + punchExtension)
        when (element) {
            GolemElement.IRON -> {
                // Spiked Iron Hammer
                val hammerRect = RectF(-22f, -6f, 22f, 22f)
                bodyPaint.color = 0xFF4A5568.toInt()
                canvas.drawRoundRect(hammerRect, 6f, 6f, bodyPaint)
                canvas.drawRoundRect(hammerRect, 6f, 6f, outlinePaint)
                // Spikes
                eyePaint.color = 0xFFFFD166.toInt()
                canvas.drawCircle(-14f, 20f, 3f, eyePaint)
                canvas.drawCircle(0f, 20f, 3f, eyePaint)
                canvas.drawCircle(14f, 20f, 3f, eyePaint)
            }
            GolemElement.FIRE -> {
                // Flaming Pyre Fist
                val fistRect = RectF(-20f, -4f, 20f, 22f)
                bodyPaint.color = 0xFFD84315.toInt()
                canvas.drawRoundRect(fistRect, 10f, 10f, bodyPaint)
                canvas.drawRoundRect(fistRect, 10f, 10f, outlinePaint)
                glowPaint.color = 0x88FF9800.toInt()
                canvas.drawCircle(0f, 10f, 24f, glowPaint)
            }
            GolemElement.ICE -> {
                // Glacial Crystal Blade
                val blade = Path().apply {
                    moveTo(0f, -4f)
                    lineTo(16f, 10f)
                    lineTo(0f, 36f)
                    lineTo(-16f, 10f)
                    close()
                }
                bodyPaint.color = 0xFF00B4D8.toInt()
                canvas.drawPath(blade, bodyPaint)
                canvas.drawPath(blade, outlinePaint)
            }
            else -> {
                // Classic Heavy Boulder Fist
                val fistRect = RectF(-18f, -4f, 18f, 22f)
                canvas.drawRoundRect(fistRect, 10f, 10f, bodyPaint)
                canvas.drawRoundRect(fistRect, 10f, 10f, outlinePaint)
            }
        }
        canvas.restore()
    }

    private fun drawOverdriveAura(canvas: Canvas, element: GolemElement, animTime: Float) {
        val auraPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 6f
            color = element.colorHex
            alpha = (100 + sin(animTime * 15f) * 60).toInt().coerceIn(0, 255)
        }
        canvas.drawCircle(0f, 10f, 95f + sin(animTime * 20f) * 10f, auraPaint)
    }
}
