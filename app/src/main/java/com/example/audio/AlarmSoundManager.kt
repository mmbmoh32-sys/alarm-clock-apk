package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

class AlarmSoundManager(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var playbackJob: Job? = null
    private var vibratorJob: Job? = null
    private var isPlaying = false

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun playAlarm(
        soundId: String,
        isVibrate: Boolean,
        isGradualVolume: Boolean,
        isPreview: Boolean = false,
        onStop: (() -> Unit)? = null
    ) {
        stop()
        isPlaying = true

        if (isVibrate) {
            startVibration(isPreview)
        }

        playbackJob = scope.launch {
            try {
                val sampleRate = 44100
                val minBufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

                val audioFormat = AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()

                val track = AudioTrack.Builder()
                    .setAudioAttributes(audioAttributes)
                    .setAudioFormat(audioFormat)
                    .setBufferSizeInBytes(minBufferSize * 2)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                track.play()

                val startTime = System.currentTimeMillis()
                val rampDurationMs = if (isGradualVolume) 15000L else 1000L
                val maxVolume = 1.0f

                var cycle = 0
                val maxCycles = if (isPreview) 3 else Int.MAX_VALUE

                while (isActive && isPlaying && cycle < maxCycles) {
                    val elapsedTime = System.currentTimeMillis() - startTime
                    val currentVolume = if (isGradualVolume) {
                        val progress = (elapsedTime.toFloat() / rampDurationMs).coerceIn(0.15f, 1.0f)
                        progress * maxVolume
                    } else {
                        maxVolume
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        track.setVolume(currentVolume)
                    }

                    val pcmData = generateSoundPattern(soundId, sampleRate, cycle)
                    track.write(pcmData, 0, pcmData.size)

                    cycle++
                    if (isPreview && cycle >= maxCycles) break
                    delay(100)
                }

                track.stop()
                track.release()
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    e.printStackTrace()
                }
            } finally {
                if (isPreview) {
                    stop()
                    onStop?.invoke()
                }
            }
        }
    }

    private fun generateSoundPattern(soundId: String, sampleRate: Int, cycle: Int): ShortArray {
        val durationSeconds = when (soundId) {
            "radar" -> 0.4
            "dawn" -> 1.2
            "waves" -> 1.5
            "gentle" -> 1.0
            "classic" -> 0.6
            else -> 0.5 // digital
        }
        val numSamples = (durationSeconds * sampleRate).toInt()
        val buffer = ShortArray(numSamples)

        val frequencies = when (soundId) {
            "radar" -> listOf(880.0, 1174.0)
            "dawn" -> listOf(523.25, 659.25, 783.99, 1046.50) // C5, E5, G5, C6 arpeggio
            "waves" -> listOf(440.0, 554.37)
            "gentle" -> listOf(587.33, 659.25, 783.99, 880.0)
            "classic" -> listOf(800.0, 950.0)
            else -> listOf(880.0, 880.0) // Digital beep
        }

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val freqIndex = ((t * frequencies.size / durationSeconds).toInt()) % frequencies.size
            val freq = frequencies[freqIndex]

            // Envelope to avoid popping and add natural tone
            val env = when (soundId) {
                "radar" -> {
                    val subT = (t * 2) % 1.0
                    (1.0 - subT).coerceIn(0.0, 1.0)
                }
                "dawn" -> {
                    sin(Math.PI * (t / durationSeconds)).coerceIn(0.0, 1.0)
                }
                "waves" -> {
                    0.5 * (1.0 + sin(2 * Math.PI * 0.8 * t))
                }
                else -> {
                    // Digital beep beep
                    val beepPhase = (t * 4) % 1.0
                    if (beepPhase < 0.6) 1.0 else 0.0
                }
            }

            val sample = sin(2.0 * Math.PI * freq * t) * env * 0.7
            buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
        }

        return buffer
    }

    private fun startVibration(isPreview: Boolean) {
        vibratorJob?.cancel()
        vibratorJob = scope.launch {
            try {
                val timings = longArrayOf(0, 400, 200, 400, 800)
                val amplitudes = intArrayOf(0, 255, 0, 255, 0)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val repeatIndex = if (isPreview) -1 else 0
                    val effect = VibrationEffect.createWaveform(timings, amplitudes, repeatIndex)
                    vibrator?.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(timings, if (isPreview) -1 else 0)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stop() {
        isPlaying = false
        playbackJob?.cancel()
        playbackJob = null
        vibratorJob?.cancel()
        vibratorJob = null
        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
