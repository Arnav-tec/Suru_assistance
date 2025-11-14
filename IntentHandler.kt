package com.example.suruassistant.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import java.util.*
import android.content.SharedPreferences
import com.example.suruassistant.service.OnlineAssistant
import android.os.Build
import android.util.Log

object IntentHandler {

    fun handle(context: Context, text: String): String {
        val lower = text.toLowerCase(Locale.getDefault())
        if (lower.isBlank()) return "Kya bola? Dobara bolo."

        if (lower.startsWith("call")) {
            val num = lower.removePrefix("call").trim()
            if (num.isNotEmpty()) {
                val i = Intent(Intent.ACTION_DIAL)
                i.data = Uri.parse("tel:$num")
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(i)
                return "Calling $num"
            }
        }

        if (lower.startsWith("whatsapp")) {
            val msg = lower.removePrefix("whatsapp").trim()
            val i = Intent(Intent.ACTION_SEND)
            i.type = "text/plain"
            i.putExtra(Intent.EXTRA_TEXT, msg)
            i.setPackage("com.whatsapp")
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            return try {
                context.startActivity(i)
                "Opening WhatsApp"
            } catch (e: Exception) {
                "WhatsApp installed nahi hai"
            }
        }

        if (lower.contains("alarm")) {
            val regex = Regex("(\d{1,2}):(\d{2})")
            val match = regex.find(lower)
            if (match != null) {
                val (hh, mm) = match.destructured
                val i = Intent(AlarmClock.ACTION_SET_ALARM)
                i.putExtra(AlarmClock.EXTRA_HOUR, hh.toInt())
                i.putExtra(AlarmClock.EXTRA_MINUTES, mm.toInt())
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(i)
                return "Setting alarm for $hh:$mm"
            }
        }

        if (lower.startsWith("note") || lower.startsWith("remember")) {
            val note = if (lower.startsWith("note")) lower.removePrefix("note").trim() else lower.removePrefix("remember").trim()
            val prefs = context.getSharedPreferences("suru_notes", Context.MODE_PRIVATE)
            prefs.edit().putString("last_note", note).apply()
            return "Note saved"
        }

        try {
            // Fallback: ask online assistant (if configured)
            val prefs = context.getSharedPreferences("suru_settings", Context.MODE_PRIVATE)
            val apiKey = prefs.getString("openai_key", null)
            val reply = OnlineAssistant.ask(apiKey, text)
            return reply
        } catch (e: Exception) {
            e.printStackTrace()
            return "Maine suna: $text. Kya karna hai?"
        }
    }
}
