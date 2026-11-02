package com.example.morningmeditation.playback

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class SessionActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val serviceIntent = Intent(context, MeditationSessionService::class.java).setAction(intent.action)
        ContextCompat.startForegroundService(context, serviceIntent)
    }
}
