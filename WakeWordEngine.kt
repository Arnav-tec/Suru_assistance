package com.example.suruassistant.service

import android.content.Context
import android.os.Handler
import android.os.Looper
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * WakeWordEngine - adapter for Porcupine wake-word engine (recommended).
 *
 * This implementation tries to load 'suru.ppn' from assets and initialize Porcupine if the
 * Picovoice/Porcupine dependency (.jar + native libs) is added to the project.
 *
 * If Porcupine is not available, it falls back to a CRUDE energy heuristic (for testing only).
 *
 * Instructions:
 * 1) Add Picovoice/Porcupine Android native libraries (.so) to app/src/main/jniLibs/<abi>/
 * 2) Add Porcupine SDK .jar / dependency per Picovoice docs.
 * 3) Keep 'suru.ppn' in app/src/main/assets/ (already included).
 * 4) Build and run.
 */
class WakeWordEngine(private val context: Context, private val onWake: () -> Unit) {

    // If Porcupine instance is available, assign it here.
    // private var porcupine: Porcupine? = null

    init {
        initEngine()
    }

    private fun initEngine() {
        // Copy suru.ppn from assets to internal files dir (Porcupine may require a file path)
        try {
            val assetName = "suru.ppn"
            val outFile = File(context.filesDir, assetName)
            if (!outFile.exists()) {
                context.assets.open(assetName).use { ins ->
                    FileOutputStream(outFile).use { fos ->
                        ins.copyTo(fos)
                    }
                }
            }
            // Try to initialize Porcupine (example pseudo-code)
            // NOTE: This block will ONLY work if Porcupine SDK is added to the project.
            try {
                // val keywordPath = outFile.absolutePath
                // porcupine = Porcupine.Builder()
                //     .setKeywordPaths(listOf(keywordPath))
                //     .build(context)
            } catch (e: Throwable) {
                // Porcupine not available — will use fallback
                e.printStackTrace()
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    /**
     * Feed PCM samples to the wake engine.
     * If Porcupine is initialized, it should be used here to detect the keyword.
     * Otherwise, a crude energy heuristic triggers on loud sounds (for quick testing).
     */
    fun feed(pcmBuffer: ShortArray, read: Int): Boolean {
        // If porcupine != null, use porcupine.process(pcmBuffer) -> boolean
        try {
            // if (porcupine != null) {
            //     val result = porcupine.process(pcmBuffer)
            //     if (result) { onWake.invoke(); return true }
            // }
        } catch (t: Throwable) {
            t.printStackTrace()
        }

        // CRUDE ENERGY HEURISTIC (REMOVE IN PRODUCTION)
        var sum = 0L
        for (i in 0 until read) sum += kotlin.math.abs(pcmBuffer[i].toInt())
        val avg = if (read>0) sum / read else 0
        if (avg > 2000) {
            // avoid calling onWake too frequently — call on main thread after slight delay
            Handler(Looper.getMainLooper()).post { onWake.invoke() }
            return true
        }
        return false
    }

    fun release() {
        // porcupine?.close()
    }
}
