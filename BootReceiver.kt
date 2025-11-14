package com.example.suruassistant.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val svcIntent = Intent(context, AssistantService::class.java)
            svcIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ContextCompat.startForegroundService(context, svcIntent)
                } else {
                    context.startService(svcIntent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
