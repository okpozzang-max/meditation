package com.example.morningmeditation.playback

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.morningmeditation.MainActivity
import com.example.morningmeditation.R
import com.example.morningmeditation.domain.SessionState
import com.example.morningmeditation.domain.Soundscape

class SessionNotification(private val context: Context) {
    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.session_notification_channel),
                NotificationManager.IMPORTANCE_LOW,
            ).apply { setSound(null, null); enableVibration(false) }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    fun build(state: SessionState): Notification {
        val paused = state is SessionState.Paused
        val minutes = (state.remainingMillis + 59_999L) / 60_000L
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText("${minutes}분 남음 · ${state.soundscape.label}")
            .setContentIntent(pendingActivity(Intent(context, MainActivity::class.java), 0))
            .setOngoing(state !is SessionState.Completed && state !is SessionState.Idle)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(
                0,
                context.getString(if (paused) R.string.resume else R.string.pause),
                actionIntent(if (paused) MeditationSessionService.ACTION_RESUME else MeditationSessionService.ACTION_PAUSE, 1),
            )
            .apply {
                if (state.soundscape != Soundscape.NONE) {
                    addAction(
                        0,
                        context.getString(if (state.soundEnabled) R.string.mute_sound else R.string.enable_sound),
                        actionIntent(
                            if (state.soundEnabled) MeditationSessionService.ACTION_SOUND_OFF else MeditationSessionService.ACTION_SOUND_ON,
                            2,
                        ),
                    )
                }
            }
            .addAction(0, context.getString(R.string.finish), actionIntent(MeditationSessionService.ACTION_FINISH, 3))
            .build()
    }

    private fun actionIntent(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, SessionActionReceiver::class.java).setAction(action)
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun pendingActivity(intent: Intent, requestCode: Int): PendingIntent = PendingIntent.getActivity(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    companion object {
        const val CHANNEL_ID = "meditation_session"
        const val NOTIFICATION_ID = 4101
    }
}
