package com.example.suruassistant.service

import android.content.Context
import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * OnlineAssistant - sends transcript text to OpenAI Chat Completions endpoint and returns a reply.
 *
 * IMPORTANT:
 * - Add your OpenAI API key to gradle.properties as OPENAI_API_KEY="sk-..."
 * - In app/build.gradle, add buildConfigField("String", "OPENAI_API_KEY", ""${OPENAI_API_KEY}"")
 *   Or use safer patterns (server-side proxy). Storing keys in app is insecure.
 *
 * This is a minimal synchronous example using OkHttp. Use coroutines in real app.
 */
object OnlineAssistant {
    private val client = OkHttpClient.Builder()
        .callTimeout(30, TimeUnit.SECONDS)
        .build()

    fun ask(openAiKey: String?, prompt: String): String {
        if (openAiKey.isNullOrEmpty()) return "Online assistant not configured."
        try {
            val url = "https://api.openai.com/v1/chat/completions"
            val json = JSONObject()
            json.put("model", "gpt-4o-mini")
            val messages = org.json.JSONArray()
            val m = JSONObject()
            m.put("role", "user")
            m.put("content", prompt)
            messages.put(m)
            json.put("messages", messages)
            json.put("max_tokens", 150)

            val body = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), json.toString())
            val req = Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Authorization", "Bearer $openAiKey")
                .build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) {
                    Log.e("OnlineAssistant", "HTTP error ${resp.code}")
                    return "Online assistant error: ${resp.code}"
                }
                val rstr = resp.body?.string() ?: ""
                // parse a simple reply if possible
                val robj = JSONObject(rstr)
                val choices = robj.optJSONArray("choices")
                if (choices != null && choices.length() > 0) {
                    val msg = choices.getJSONObject(0).optJSONObject("message")
                    if (msg != null) {
                        return msg.optString("content", "No reply")
                    }
                }
                return "No reply from assistant."
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return "Error contacting online assistant."
        }
    }
}
