package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.PI
import kotlin.math.sin

class SoundManager(context: Context) {
    var isSoundEnabled by mutableStateOf(true)
    var isHapticsEnabled by mutableStateOf(true)

    private val soundPool: SoundPool
    private val soundIds: MutableMap<String, Int> = mutableMapOf()

    // Pentatonic scale pitch multipliers for musical combo chains
    private val comboScale = floatArrayOf(
        1.000f, // Root (E5)
        1.122f, // F#5
        1.260f, // G#5
        1.414f, // B5
        1.587f, // C#6
        1.782f, // E6
        2.000f  // Octave
    )

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(32)
            .setAudioAttributes(audioAttributes)
            .build()

        // Generate synthesized high quality arcade sound effects into cache directory
        val soundDir = File(context.cacheDir, "sounds").apply { if (!exists()) mkdirs() }
        loadOrGenerateSounds(soundDir)
    }

    private fun loadOrGenerateSounds(soundDir: File) {
        val soundGenerators = mapOf(
            "click" to { generateClickWav(File(soundDir, "click.wav")) },
            "hit" to { generateHitWav(File(soundDir, "hit.wav")) },
            "break" to { generateBreakWav(File(soundDir, "break.wav")) },
            "bounce" to { generateBounceWav(File(soundDir, "bounce.wav")) },
            "collect" to { generateCollectWav(File(soundDir, "collect.wav")) },
            "laser" to { generateLaserWav(File(soundDir, "laser.wav")) },
            "bomb" to { generateBombWav(File(soundDir, "bomb.wav")) },
            "launch" to { generateLaunchWav(File(soundDir, "launch.wav")) },
            "victory" to { generateVictoryWav(File(soundDir, "victory.wav")) }
        )

        for ((name, generator) in soundGenerators) {
            val file = generator()
            if (file.exists() && file.length() > 44) {
                val soundId = soundPool.load(file.absolutePath, 1)
                soundIds[name] = soundId
            }
        }
    }

    private fun generateWav(
        file: File,
        sampleRate: Int = 44100,
        durationSeconds: Float,
        sampleProvider: (sampleIndex: Int, totalSamples: Int, t: Float) -> Short
    ): File {
        if (file.exists() && file.length() > 44) return file
        try {
            val totalSamples = (sampleRate * durationSeconds).toInt()
            val dataSize = totalSamples * 2
            val totalSize = 36 + dataSize

            val fos = FileOutputStream(file)
            val bos = BufferedOutputStream(fos)

            // Header (44 bytes)
            bos.write("RIFF".toByteArray(Charsets.US_ASCII))
            bos.write(intToBytes(totalSize))
            bos.write("WAVE".toByteArray(Charsets.US_ASCII))
            bos.write("fmt ".toByteArray(Charsets.US_ASCII))
            bos.write(intToBytes(16)) // Subchunk1Size
            bos.write(shortToBytes(1.toShort())) // AudioFormat (1 = PCM)
            bos.write(shortToBytes(1.toShort())) // NumChannels (1 = Mono)
            bos.write(intToBytes(sampleRate))
            bos.write(intToBytes(sampleRate * 2)) // ByteRate
            bos.write(shortToBytes(2.toShort())) // BlockAlign
            bos.write(shortToBytes(16.toShort())) // BitsPerSample
            bos.write("data".toByteArray(Charsets.US_ASCII))
            bos.write(intToBytes(dataSize))

            // PCM samples
            for (i in 0 until totalSamples) {
                val t = i.toFloat() / sampleRate.toFloat()
                val sample = sampleProvider(i, totalSamples, t)
                bos.write(sample.toInt() and 0xFF)
                bos.write((sample.toInt() shr 8) and 0xFF)
            }

            bos.flush()
            bos.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return file
    }

    private fun intToBytes(value: Int): ByteArray = byteArrayOf(
        (value and 0xFF).toByte(),
        ((value shr 8) and 0xFF).toByte(),
        ((value shr 16) and 0xFF).toByte(),
        ((value shr 24) and 0xFF).toByte()
    )

    private fun shortToBytes(value: Short): ByteArray = byteArrayOf(
        (value.toInt() and 0xFF).toByte(),
        ((value.toInt() shr 8) and 0xFF).toByte()
    )

    private fun generateClickWav(file: File): File = generateWav(file, durationSeconds = 0.045f) { _, _, t ->
        val freq = 850.0 - (500.0 * (t / 0.045f))
        val env = Math.exp(-t * 70.0)
        val sine = sin(2.0 * PI * freq * t)
        (sine * env * 24000.0).toInt().coerceIn(-32768, 32767).toShort()
    }

    private fun generateHitWav(file: File): File = generateWav(file, durationSeconds = 0.08f) { _, _, t ->
        val baseFreq = 520.0
        val env = Math.exp(-t * 40.0)
        val sine = sin(2.0 * PI * baseFreq * t) + 0.25 * sin(2.0 * PI * baseFreq * 2.0 * t)
        (sine * env * 26000.0).toInt().coerceIn(-32768, 32767).toShort()
    }

    private fun generateBreakWav(file: File): File = generateWav(file, durationSeconds = 0.15f) { i, _, t ->
        val freq = 800.0 - (650.0 * (t / 0.15f))
        val env = Math.exp(-t * 22.0)
        val noise = (kotlin.random.Random.nextFloat() * 2f - 1f) * 0.45f
        val tone = sin(2.0 * PI * freq * t)
        ((tone + noise) * env * 28000.0).toInt().coerceIn(-32768, 32767).toShort()
    }

    private fun generateBounceWav(file: File): File = generateWav(file, durationSeconds = 0.04f) { _, _, t ->
        val freq = 280.0 - (100.0 * (t / 0.04f))
        val env = Math.exp(-t * 75.0)
        val sine = sin(2.0 * PI * freq * t)
        (sine * env * 18000.0).toInt().coerceIn(-32768, 32767).toShort()
    }

    private fun generateCollectWav(file: File): File = generateWav(file, durationSeconds = 0.16f) { _, _, t ->
        val (freq, localT) = if (t < 0.08f) {
            Pair(659.25, t)
        } else {
            Pair(1046.50, t - 0.08f)
        }
        val env = Math.exp(-localT * 35.0)
        val sine = sin(2.0 * PI * freq * t)
        (sine * env * 26000.0).toInt().coerceIn(-32768, 32767).toShort()
    }

    private fun generateLaserWav(file: File): File = generateWav(file, durationSeconds = 0.12f) { _, _, t ->
        val freq = 1500.0 - (1300.0 * (t / 0.12f))
        val env = Math.exp(-t * 18.0)
        val phase = freq * t
        val saw = (2.0 * (phase - Math.floor(phase + 0.5)))
        (saw * env * 22000.0).toInt().coerceIn(-32768, 32767).toShort()
    }

    private fun generateBombWav(file: File): File = generateWav(file, durationSeconds = 0.28f) { _, _, t ->
        val subFreq = 140.0 - (110.0 * (t / 0.28f))
        val env = Math.exp(-t * 12.0)
        val noise = (kotlin.random.Random.nextFloat() * 2f - 1f) * 0.6f
        val sub = sin(2.0 * PI * subFreq * t)
        ((sub + noise) * env * 28000.0).toInt().coerceIn(-32768, 32767).toShort()
    }

    private fun generateLaunchWav(file: File): File = generateWav(file, durationSeconds = 0.14f) { _, _, t ->
        val freq = 220.0 + (680.0 * (t / 0.14f))
        val env = sin(Math.PI * (t / 0.14f))
        val sine = sin(2.0 * PI * freq * t)
        (sine * env * 22000.0).toInt().coerceIn(-32768, 32767).toShort()
    }

    private fun generateVictoryWav(file: File): File = generateWav(file, durationSeconds = 0.45f) { _, _, t ->
        val (freq, localT) = when {
            t < 0.10f -> Pair(523.25, t)
            t < 0.20f -> Pair(659.25, t - 0.10f)
            t < 0.30f -> Pair(783.99, t - 0.20f)
            else -> Pair(1046.50, t - 0.30f)
        }
        val env = Math.exp(-localT * 18.0)
        val sine = sin(2.0 * PI * freq * t)
        (sine * env * 28000.0).toInt().coerceIn(-32768, 32767).toShort()
    }

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private var lastHitHapticTime = 0L
    private var lastBreakHapticTime = 0L
    private var lastExplosionHapticTime = 0L
    private var lastHitSoundTime = 0L
    private var lastBreakSoundTime = 0L
    private var lastLaserSoundTime = 0L
    private var lastBombSoundTime = 0L
    private var lastBounceSoundTime = 0L

    fun playHitSound(comboIndex: Int) {
        if (!isSoundEnabled) return
        val now = android.os.SystemClock.uptimeMillis()
        if (now - lastHitSoundTime < 16L) return
        lastHitSoundTime = now
        val id = soundIds["hit"] ?: return
        val noteIdx = (comboIndex.coerceAtLeast(0)) % comboScale.size
        val octaveShift = (comboIndex / comboScale.size).coerceAtMost(2) * 0.14f
        val pitch = (comboScale[noteIdx] + octaveShift).coerceIn(0.75f, 2.0f)
        soundPool.play(id, 0.90f, 0.90f, 1, 0, pitch)
    }

    fun playBounceSound() {
        if (!isSoundEnabled) return
        val now = android.os.SystemClock.uptimeMillis()
        if (now - lastBounceSoundTime < 35L) return
        lastBounceSoundTime = now
        val id = soundIds["bounce"] ?: soundIds["click"] ?: return
        soundPool.play(id, 0.45f, 0.45f, 1, 0, 1.2f)
    }

    fun playBreakSound() {
        if (!isSoundEnabled) return
        val now = android.os.SystemClock.uptimeMillis()
        if (now - lastBreakSoundTime < 28L) return
        lastBreakSoundTime = now
        val id = soundIds["break"] ?: return
        val randomPitch = 1.0f + (Math.random().toFloat() * 0.20f)
        soundPool.play(id, 1.0f, 1.0f, 2, 0, randomPitch)
    }

    fun playCollectBallSound() {
        if (!isSoundEnabled) return
        val id = soundIds["collect"] ?: return
        soundPool.play(id, 1.0f, 1.0f, 3, 0, 1.25f)
    }

    fun playLaserSound() {
        if (!isSoundEnabled) return
        val now = android.os.SystemClock.uptimeMillis()
        if (now - lastLaserSoundTime < 35L) return
        lastLaserSoundTime = now
        val id = soundIds["laser"] ?: return
        val pitch = 0.95f + (Math.random().toFloat() * 0.18f)
        soundPool.play(id, 1.0f, 1.0f, 3, 0, pitch)
    }

    fun playBombSound() {
        if (!isSoundEnabled) return
        val now = android.os.SystemClock.uptimeMillis()
        if (now - lastBombSoundTime < 60L) return
        lastBombSoundTime = now
        val id = soundIds["bomb"] ?: return
        soundPool.play(id, 1.0f, 1.0f, 3, 0, 0.92f)
    }

    fun playLaunchSound() {
        if (!isSoundEnabled) return
        val id = soundIds["launch"] ?: return
        soundPool.play(id, 0.85f, 0.85f, 1, 0, 1.05f)
    }

    fun playVictorySound() {
        if (!isSoundEnabled) return
        val id = soundIds["victory"] ?: return
        soundPool.play(id, 1f, 1f, 3, 0, 1.0f)
    }

    fun playClickSound() {
        if (!isSoundEnabled) return
        val id = soundIds["click"] ?: return
        soundPool.play(id, 0.85f, 0.85f, 1, 0, 1.0f)
    }

    fun triggerHitHaptic() {
        if (!isHapticsEnabled || vibrator?.hasVibrator() != true) return
        val now = android.os.SystemClock.uptimeMillis()
        if (now - lastHitHapticTime < 50L) return
        lastHitHapticTime = now
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(12, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(12)
            }
        } catch (_: Exception) {}
    }

    fun triggerBreakHaptic() {
        if (!isHapticsEnabled || vibrator?.hasVibrator() != true) return
        val now = android.os.SystemClock.uptimeMillis()
        if (now - lastBreakHapticTime < 60L) return
        lastBreakHapticTime = now
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(35)
            }
        } catch (_: Exception) {}
    }

    fun triggerExplosionHaptic() {
        if (!isHapticsEnabled || vibrator?.hasVibrator() != true) return
        val now = android.os.SystemClock.uptimeMillis()
        if (now - lastExplosionHapticTime < 100L) return
        lastExplosionHapticTime = now
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(80)
            }
        } catch (_: Exception) {}
    }
}
