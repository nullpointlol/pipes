package com.pipes.game.core.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * Sintetizador procedural autónomo en tiempo real para Android.
 * Genera tonos armónicos pentatónicos sin necesidad de archivos de audio externos.
 */
class SoundController(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    var isEnabled: Boolean = true

    // Escala pentatónica en Hertz (Do, Re, Mi, Sol, La en octavas 4, 5 y 6)
    private val pentatonicScale = doubleArrayOf(
        261.63, 293.66, 329.63, 392.00, 440.00, // C4..A4
        523.25, 587.33, 659.25, 783.99, 880.00, // C5..A5
        1046.50, 1174.66                         // C6..D6
    )

    fun playRotateClick() {
        if (!isEnabled) return
        scope.launch {
            generateTone(frequency = 320.0, durationMs = 45, amplitude = 0.15f, dropTo = 140.0)
        }
    }

    fun playEnergyStep(percent: Int) {
        if (!isEnabled) return
        scope.launch {
            val idx = ((percent / 100.0) * (pentatonicScale.size - 1)).toInt().coerceIn(0, pentatonicScale.size - 1)
            val freq = pentatonicScale[idx]
            generateTone(frequency = freq, durationMs = 180, amplitude = 0.22f)
        }
    }

    fun playVictory() {
        if (!isEnabled) return
        scope.launch {
            val chord = doubleArrayOf(523.25, 659.25, 783.99, 1046.50) // C-E-G-C
            chord.forEachIndexed { i, freq ->
                launch {
                    kotlinx.coroutines.delay(i * 85L)
                    generateTone(frequency = freq, durationMs = 450, amplitude = 0.28f)
                }
            }
        }
    }

    private fun generateTone(frequency: Double, durationMs: Int, amplitude: Float, dropTo: Double? = null) {
        val sampleRate = 44100
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val generatedSnd = ShortArray(numSamples)

        var currentFreq = frequency
        val freqStep = if (dropTo != null) (dropTo - frequency) / numSamples else 0.0

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val envelope = (1.0 - progress) * amplitude // Envelope lineal decreciente

            val sample = (sin(2.0 * Math.PI * i / (sampleRate / currentFreq)) * envelope * Short.MAX_VALUE).toInt()
            generatedSnd[i] = sample.toShort()
            currentFreq += freqStep
        }

        try {
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(numSamples * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(generatedSnd, 0, numSamples)
            audioTrack.play()
            Thread.sleep(durationMs.toLong() + 20)
            audioTrack.release()
        } catch (_: Exception) {
            // Manejo silencioso en dispositivos sin hardware de audio activo
        }
    }
}
