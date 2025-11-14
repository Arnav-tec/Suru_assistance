package com.example.suruassistant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var switchEnable: Switch
    private lateinit var btnStart: Button

    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        // no-op; user will see UI
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        switchEnable = findViewById(R.id.switchEnable)
        btnStart = findViewById(R.id.btnStartService)

        btnStart.setOnClickListener {
            if (!checkMicPermission()) {
                requestNeededPermissions()
                return@setOnClickListener
            }
            startService()
        }

        switchEnable.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) startService() else stopService()
        }
    }

    private fun startService() {
        val intent = Intent(this, com.example.suruassistant.service.AssistantService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(this, intent)
        } else startService(intent)
        tvStatus.text = "Service running"
    }

    private fun stopService() {
        val intent = Intent(this, com.example.suruassistant.service.AssistantService::class.java)
        stopService(intent)
        tvStatus.text = "Service stopped"
    }

    private fun checkMicPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestNeededPermissions() {
        val perms = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // FOREGROUND_SERVICE permission is normal; still include for clarity
        }
        perms.add(Manifest.permission.WAKE_LOCK)
        perms.add(Manifest.permission.INTERNET)
        requestPermissions.launch(perms.toTypedArray())
    }
}
