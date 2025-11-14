package com.example.suruassistant.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.speech.tts.TextToSpeech
import androidx.core.app.NotificationCompat
import com.example.suruassistant.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class AssistantService : Service(), TextToSpeech.OnInitListener {
    private val NOTIF_ID = 1001
    private val CHANNEL_ID = "suru_assistant_channel"

    private var running = false
    private var audioThread: Thread? = null

    private lateinit var tts: TextToSpeech
    private lateinit var wakeEngine: WakeWordEngine
    private lateinit var sttManager: STTManager

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification = buildNotification("Suru assistant running")
        startForeground(NOTIF_ID, notification)

        tts = TextToSpeech(this, this)
        wakeEngine = WakeWordEngine(this) { onWakeDetected() }
        sttManager = STTManager(this) { transcript -> onTranscript(transcript) }

        startAudioLoop()
    }

    override fun onDestroy() {
        running = false
        audioThread?.interrupt()
        wakeEngine.release()
        sttManager.release()
        tts.shutdown()
        stopForeground(true)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(CHANNEL_ID, "Suru Assistant", NotificationManager.IMPORTANCE_LOW)
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(text: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Suru Assistant")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .build()
    }

    private fun startAudioLoop() {
        running = true
        audioThread = Thread {
            val sampleRate = 16000
            val minBuf = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
            val recorder = AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, sampleRate,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, minBuf)

            val buffer = ShortArray(512)
            recorder.startRecording()
            try {
                while (running && !Thread.interrupted()) {
                    val read = recorder.read(buffer, 0, buffer.size)
                    if (read > 0) {
                        val detected = wakeEngine.feed(buffer, read)
                        if (detected) {
                            playBeep()
                            sttManager.startListeningOnce()
                            Thread.sleep(800)
                        }
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            } finally {
                recorder.stop()
                recorder.release()
            }
        }
        audioThread?.start()
    }

    private fun onWakeDetected() {
        CoroutineScope(Dispatchers.Main).launch {
            startForeground(NOTIF_ID, buildNotification("Listening..."))
        }
    }

    private fun onTranscript(text: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val response = IntentHandler.handle(this@AssistantService, text)
            speak(response)
            startForeground(NOTIF_ID, buildNotification("Suru assistant running"))
        }
    }

    private fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "suru_tts")
    }

    private fun playBeep() {
        try {
            android.media.ToneGenerator(android.media.AudioManager.STREAM_NOTIFICATION, 80)
                .startTone(android.media.ToneGenerator.TONE_PROP_ACK, 120)
        } catch (e: Exception) { }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.getDefault()
        }
    }
}
