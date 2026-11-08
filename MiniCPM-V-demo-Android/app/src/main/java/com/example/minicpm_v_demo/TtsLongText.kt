package com.example.minicpm_v_demo

import java.io.File
import java.io.RandomAccessFile

/** Sentence-aware, bounded input chunks for on-device TTS. */
internal object TtsLongText {
    fun split(text: String, limit: Int = 280): List<String> {
        require(limit in 32..1000)
        val normalized = text.trim()
        if (normalized.isEmpty()) return emptyList()
        val result = mutableListOf<String>()
        var remaining = normalized
        while (remaining.isNotEmpty()) {
            if (remaining.length <= limit) {
                result.add(remaining)
                break
            }
            val boundary = (1..limit).lastOrNull { i ->
                val c = remaining[i - 1]
                c == '။' || c == '!' || c == '?' || c == '.' || c == '\n'
            } ?: (1..limit).lastOrNull { i -> remaining[i - 1].isWhitespace() } ?: limit
            result.add(remaining.substring(0, boundary).trim())
            remaining = remaining.substring(boundary).trimStart()
        }
        return result.filter { it.isNotEmpty() }
    }

    /** Concatenates compatible canonical PCM WAV files without loading audio into RAM. */
    fun joinWavs(parts: List<File>, destination: File) {
        require(parts.isNotEmpty())
        val header = ByteArray(44)
        var total = 0L
        parts.forEachIndexed { index, file ->
            RandomAccessFile(file, "r").use { raf ->
                require(raf.length() >= 44) { "Invalid WAV part $index" }
                val h = ByteArray(44)
                raf.readFully(h)
                require(String(h, 0, 4, Charsets.US_ASCII) == "RIFF" &&
                    String(h, 8, 4, Charsets.US_ASCII) == "WAVE" &&
                    String(h, 12, 4, Charsets.US_ASCII) == "fmt " &&
                    String(h, 36, 4, Charsets.US_ASCII) == "data") { "Unsupported WAV layout" }
                if (index == 0) h.copyInto(header)
                else require(h.sliceArray(20..35).contentEquals(header.sliceArray(20..35))) {
                    "WAV formats differ"
                }
                total += raf.length() - 44
            }
        }
        require(total <= Int.MAX_VALUE - 36L) { "WAV too large" }
        fun put32(offset: Int, value: Int) {
            for (i in 0..3) header[offset + i] = (value ushr (i * 8)).toByte()
        }
        put32(4, (total + 36).toInt())
        put32(40, total.toInt())
        destination.outputStream().buffered().use { output ->
            output.write(header)
            parts.forEach { file ->
                file.inputStream().buffered().use { input ->
                    var skipped = 0L
                    while (skipped < 44) {
                        val n = input.skip(44 - skipped)
                        require(n > 0) { "Short WAV header" }
                        skipped += n
                    }
                    input.copyTo(output)
                }
            }
        }
    }
}
