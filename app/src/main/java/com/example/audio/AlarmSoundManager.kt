package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
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
                val maxCycles = if (isPreview) 4 else Int.MAX_VALUE

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
                    delay(80)
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
            // Athan styles
            "athan_makkah" -> 1.8 // Maqam Bayati solemn resonant tone
            "athan_madinah" -> 1.6 // Maqam Rast gentle melody
            "athan_aqsa" -> 1.8 // Maqam Hijaz soulful melody
            "athan_cairo" -> 1.7 // Traditional Egyptian melody
            "athan_gentle" -> 1.2 // Takbeerat chime
            else -> 0.5 // digital
        }
        val numSamples = (durationSeconds * sampleRate).toInt()
        val buffer = ShortArray(numSamples)

        val frequencies = when (soundId) {
            "radar" -> listOf(880.0, 1174.0)
            "dawn" -> listOf(523.25, 659.25, 783.99, 1046.50)
            "waves" -> listOf(440.0, 554.37)
            "gentle" -> listOf(587.33, 659.25, 783.99, 880.0)
            "classic" -> listOf(800.0, 950.0)
            // Distinctive Maqam vocal approximations for Muadhin sounds
            "athan_makkah" -> listOf(293.66, 329.63, 349.23, 392.00, 349.23, 293.66) // D4, E4, F4, G4 Bayati
            "athan_madinah" -> listOf(261.63, 293.66, 329.63, 349.23, 392.00) // C4, D4, E4, F4 Rast
            "athan_aqsa" -> listOf(293.66, 311.13, 369.99, 392.00, 311.13, 293.66) // D4, Eb4, F#4, G4 Hijaz
            "athan_cairo" -> listOf(349.23, 392.00, 415.30, 523.25, 392.00) // Traditional Cairo
            "athan_gentle" -> listOf(392.00, 440.00, 523.25, 659.25) // Peaceful Takbeerat
            else -> listOf(880.0, 880.0) // Digital beep
        }

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val freqIndex = ((t * frequencies.size / durationSeconds).toInt()) % frequencies.size
            val baseFreq = frequencies[freqIndex]

            val isAthan = soundId.startsWith("athan_")

            val env = when {
                soundId == "radar" -> {
                    val subT = (t * 2) % 1.0
                    (1.0 - subT).coerceIn(0.0, 1.0)
                }
                soundId == "dawn" -> sin(Math.PI * (t / durationSeconds)).coerceIn(0.0, 1.0)
                soundId == "waves" -> 0.5 * (1.0 + sin(2 * Math.PI * 0.8 * t))
                isAthan -> {
                    // Smooth singing envelope with vibrato
                    val segmentDuration = durationSeconds / frequencies.size
                    val segmentT = (t % segmentDuration) / segmentDuration
                    sin(Math.PI * segmentT).coerceIn(0.0, 1.0)
                }
                else -> {
                    val beepPhase = (t * 4) % 1.0
                    if (beepPhase < 0.6) 1.0 else 0.0
                }
            }

            // Add subtle warm overtone for athan voices
            val vibrato = if (isAthan) 1.0 + 0.015 * sin(2 * Math.PI * 5.5 * t) else 1.0
            val harmonic = if (isAthan) 0.3 * sin(2.0 * Math.PI * (baseFreq * 2.0) * vibrato * t) else 0.0
            val fundamental = sin(2.0 * Math.PI * baseFreq * vibrato * t)

            val sample = (fundamental + harmonic) * env * 0.65
            buffer[i] = (sample.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
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
