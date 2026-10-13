package com.artificer.buildagolem.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.sin

object GameAudio {

    private val executor = Executors.newSingleThreadExecutor()
    var soundEnabled: Boolean = true
    private const val SAMPLE_RATE = 22050

    fun playClick() {
        if (!soundEnabled) return
        executor.execute {
            generateTone(freqStart = 800f, freqEnd = 1200f, durationMs = 35, volume = 0.3f, isSine = true)
        }
    }

    fun playEquip() {
        if (!soundEnabled) return
        executor.execute {
            generateTone(freqStart = 440f, freqEnd = 880f, durationMs = 90, volume = 0.5f, isSine = true)
        }
    }

    fun playHammerAnvil() {
        if (!soundEnabled) return
        executor.execute {
            // Metallic chime + harsh transient
            generateMetallicClang(freq = 1400f, durationMs = 180, volume = 0.7f)
        }
    }

    fun playPunchHit() {
        if (!soundEnabled) return
        executor.execute {
            generateNoiseBurst(durationMs = 90, volume = 0.8f, lowPass = true)
        }
    }

    fun playCoreBlast() {
        if (!soundEnabled) return
        executor.execute {
            generateTone(freqStart = 900f, freqEnd = 220f, durationMs = 250, volume = 0.7f, isSine = false)
        }
    }

    fun playShieldBlock() {
        if (!soundEnabled) return
        executor.execute {
            generateTone(freqStart = 320f, freqEnd = 480f, durationMs = 120, volume = 0.6f, isSine = true)
        }
    }

    fun playOverdrive() {
        if (!soundEnabled) return
        executor.execute {
            generateTone(freqStart = 200f, freqEnd = 950f, durationMs = 400, volume = 0.85f, isSine = true)
        }
    }

    fun playVictory() {
        if (!soundEnabled) return
        executor.execute {
            generateFanfare()
        }
    }

    fun playDefeat() {
        if (!soundEnabled) return
        executor.execute {
            generateTone(freqStart = 400f, freqEnd = 120f, durationMs = 450, volume = 0.7f, isSine = false)
        }
    }

    private fun generateTone(
        freqStart: Float,
        freqEnd: Float,
        durationMs: Int,
        volume: Float,
        isSine: Boolean
    ) {
        try {
            val numSamples = (SAMPLE_RATE * (durationMs / 1000f)).toInt()
            val buffer = ShortArray(numSamples)
            var currentPhase = 0.0

            for (i in 0 until numSamples) {
                val progress = i.toDouble() / numSamples
                val currentFreq = freqStart + (freqEnd - freqStart) * progress
                val phaseIncrement = 2.0 * PI * currentFreq / SAMPLE_RATE
                currentPhase += phaseIncrement

                val sample = if (isSine) {
                    sin(currentPhase)
                } else {
                    // Triangle / Saw hybrid
                    (2.0 * (currentPhase % (2.0 * PI)) / (2.0 * PI) - 1.0)
                }

                // Envelope: quick attack, smooth linear decay
                val envelope = (1.0 - progress) * (if (progress < 0.05) progress / 0.05 else 1.0)
                val value = (sample * envelope * volume * Short.MAX_VALUE).toInt()
                buffer[i] = value.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }

            writeAndPlay(buffer)
        } catch (_: Exception) {}
    }

    private fun generateMetallicClang(freq: Float, durationMs: Int, volume: Float) {
        try {
            val numSamples = (SAMPLE_RATE * (durationMs / 1000f)).toInt()
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val t = i.toDouble() / SAMPLE_RATE
                val progress = i.toDouble() / numSamples

                // Inharmonic partials of metal chime
                val p1 = sin(2.0 * PI * freq * t)
                val p2 = sin(2.0 * PI * (freq * 1.58) * t) * 0.6
                val p3 = sin(2.0 * PI * (freq * 2.32) * t) * 0.4
                val noise = (Math.random() * 2.0 - 1.0) * (1.0 - progress.coerceAtMost(0.2) / 0.2) * 0.5

                val mixed = (p1 + p2 + p3 + noise) / 2.5
                val decay = Math.exp(-progress * 7.0)
                val value = (mixed * decay * volume * Short.MAX_VALUE).toInt()
                buffer[i] = value.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }

            writeAndPlay(buffer)
        } catch (_: Exception) {}
    }

    private fun generateNoiseBurst(durationMs: Int, volume: Float, lowPass: Boolean) {
        try {
            val numSamples = (SAMPLE_RATE * (durationMs / 1000f)).toInt()
            val buffer = ShortArray(numSamples)
            var lastSample = 0.0

            for (i in 0 until numSamples) {
                val progress = i.toDouble() / numSamples
                val raw = Math.random() * 2.0 - 1.0

                // Simple 1-pole filter
                val filtered = if (lowPass) {
                    lastSample = lastSample * 0.75 + raw * 0.25
                    lastSample
                } else raw

                val envelope = Math.pow(1.0 - progress, 2.0)
                val value = (filtered * envelope * volume * Short.MAX_VALUE).toInt()
                buffer[i] = value.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }

            writeAndPlay(buffer)
        } catch (_: Exception) {}
    }

    private fun generateFanfare() {
        val notes = listOf(440f, 554.37f, 659.25f, 880f)
        for (freq in notes) {
            generateTone(freqStart = freq, freqEnd = freq, durationMs = 110, volume = 0.6f, isSine = true)
            Thread.sleep(115)
        }
    }

    private fun writeAndPlay(buffer: ShortArray) {
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(buffer.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        track.write(buffer, 0, buffer.size)
        track.play()
        executor.execute {
            try {
                Thread.sleep((buffer.size * 1000L / SAMPLE_RATE) + 50)
                track.release()
            } catch (_: Exception) {}
        }
    }
}
