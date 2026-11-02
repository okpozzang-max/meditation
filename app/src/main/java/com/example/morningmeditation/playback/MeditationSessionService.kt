package com.example.morningmeditation.playback

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import androidx.core.app.ServiceCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.morningmeditation.domain.MeditationDuration
import com.example.morningmeditation.domain.MeditationTimer
import com.example.morningmeditation.domain.MonotonicClock
import com.example.morningmeditation.domain.SessionState
import com.example.morningmeditation.domain.Soundscape
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MeditationSessionService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var timer: MeditationTimer? = null
    private var player: ExoPlayer? = null
    private var ticker: Job? = null
    private var notification: SessionNotification? = null
    private var soundscape: Soundscape = Soundscape.NONE
    private var wakeLock: PowerManager.WakeLock? = null
    private var lastNotificationKey: String? = null

    override fun onCreate() {
        super.onCreate()
        notification = SessionNotification(this)
        player = ExoPlayer.Builder(this).build().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true,
            )
            repeatMode = Player.REPEAT_MODE_ONE
            addListener(object : Player.Listener {
                override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                    if (!playWhenReady && reason == Player.PLAY_WHEN_READY_CHANGE_REASON_AUDIO_FOCUS_LOSS) {
                        timer?.pause()
                        releaseWakeLock()
                        publishCurrent()
                    }
                }

                override fun onPlaybackSuppressionReasonChanged(playbackSuppressionReason: Int) {
                    if (playbackSuppressionReason == Player.PLAYBACK_SUPPRESSION_REASON_TRANSIENT_AUDIO_FOCUS_LOSS) {
                        timer?.pause()
                        releaseWakeLock()
                        player?.pause()
                        publishCurrent()
                    }
                }
            })
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startSession(intent)
            ACTION_PAUSE -> pauseSession()
            ACTION_RESUME -> resumeSession()
            ACTION_SOUND_OFF -> setSoundEnabled(false)
            ACTION_SOUND_ON -> setSoundEnabled(true)
            ACTION_FINISH -> finishSession()
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startSession(intent: Intent) {
        val duration = enumOrDefault(intent.getStringExtra(EXTRA_DURATION), MeditationDuration.FIVE_MINUTES)
        soundscape = enumOrDefault(intent.getStringExtra(EXTRA_SOUNDSCAPE), Soundscape.FOREST)
        val newTimer = MeditationTimer(MonotonicClock { SystemClock.elapsedRealtime() })
        newTimer.start(duration, soundscape)
        timer = newTimer
        val initial = newTimer.stateAt(SystemClock.elapsedRealtime())
        SessionStateStore.publish(initial)
        enterForeground(initial)
        acquireWakeLock(duration.millis)
        startOrPausePlayer(initial.soundEnabled)
        ticker?.cancel()
        ticker = serviceScope.launch {
            while (isActive) {
                delay(250L)
                val current = timer?.stateAt(SystemClock.elapsedRealtime()) ?: break
                SessionStateStore.publish(current)
                notification?.let { updateForeground(current) }
                if (current is SessionState.Completed) {
                    completeSession()
                    break
                }
            }
        }
    }

    private fun pauseSession() {
        timer?.pause()
        player?.pause()
        releaseWakeLock()
        publishCurrent()
    }

    private fun resumeSession() {
        timer?.resume()
        publishCurrent()
        val current = SessionStateStore.state.value
        acquireWakeLock(current.remainingMillis)
        startOrPausePlayer(current.soundEnabled)
    }

    private fun setSoundEnabled(enabled: Boolean) {
        if (soundscape == Soundscape.NONE) return
        timer?.setSoundEnabled(enabled)
        val current = timer?.stateAt(SystemClock.elapsedRealtime()) ?: return
        SessionStateStore.publish(current)
        startOrPausePlayer(enabled && current !is SessionState.Paused)
        updateForeground(current)
    }

    private fun startOrPausePlayer(shouldPlay: Boolean) {
        val resource = AudioCatalog.resourceFor(soundscape) ?: return
        val exoPlayer = player ?: return
        if (shouldPlay) {
            if (exoPlayer.mediaItemCount == 0 || exoPlayer.currentMediaItemIndex < 0) {
                exoPlayer.setMediaItem(MediaItem.fromUri(Uri.parse("android.resource://$packageName/$resource")))
                exoPlayer.prepare()
            }
            exoPlayer.play()
        } else {
            exoPlayer.pause()
        }
    }

    private fun publishCurrent() {
        val current = timer?.stateAt(SystemClock.elapsedRealtime()) ?: return
        SessionStateStore.publish(current)
        updateForeground(current)
    }

    private fun enterForeground(state: SessionState) {
        val notification = notification?.build(state) ?: return
        lastNotificationKey = notificationKey(state)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val type = if (soundscape == Soundscape.NONE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            }
            ServiceCompat.startForeground(this, SessionNotification.NOTIFICATION_ID, notification, type)
        } else {
            startForeground(SessionNotification.NOTIFICATION_ID, notification)
        }
    }

    private fun acquireWakeLock(remainingMillis: Long) {
        releaseWakeLock()
        if (remainingMillis <= 0L) return
        wakeLock = getSystemService(PowerManager::class.java)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "morningmeditation:session")
            .apply {
                setReferenceCounted(false)
                acquire(remainingMillis + 10_000L)
            }
    }

    private fun releaseWakeLock() {
        wakeLock?.let { if (it.isHeld) it.release() }
        wakeLock = null
    }

    private fun updateForeground(state: SessionState) {
        val key = notificationKey(state)
        if (lastNotificationKey == key) return
        lastNotificationKey = key
        notification?.build(state)?.let { notification ->
            getSystemService(android.app.NotificationManager::class.java)
                .notify(SessionNotification.NOTIFICATION_ID, notification)
        }
    }

    private fun notificationKey(state: SessionState): String =
        "${(state.remainingMillis + 59_999L) / 60_000L}:${state is SessionState.Paused}:${state.soundEnabled}:${state.soundscape}"

    private fun completeSession() {
        ticker?.cancel()
        serviceScope.launch {
            val activePlayer = player
            if (activePlayer != null && activePlayer.isPlaying) {
                val initialVolume = activePlayer.volume
                repeat(8) { step ->
                    activePlayer.volume = initialVolume * (7 - step) / 8f
                    delay(100L)
                }
                activePlayer.volume = initialVolume
                activePlayer.pause()
            }
            releaseWakeLock()
            ServiceCompat.stopForeground(this@MeditationSessionService, ServiceCompat.STOP_FOREGROUND_REMOVE)
            delay(1_500L)
            SessionStateStore.publish(SessionState.Idle)
            stopSelf()
        }
    }

    private fun finishSession() {
        ticker?.cancel()
        timer?.finish()
        player?.stop()
        releaseWakeLock()
        SessionStateStore.publish(SessionState.Idle)
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        ticker?.cancel()
        serviceScope.cancel()
        player?.release()
        player = null
        releaseWakeLock()
        timer = null
        super.onDestroy()
    }

    private inline fun <reified T : Enum<T>> enumOrDefault(name: String?, default: T): T =
        enumValues<T>().firstOrNull { it.name == name } ?: default

    companion object {
        const val ACTION_START = "com.example.morningmeditation.action.START"
        const val ACTION_PAUSE = "com.example.morningmeditation.action.PAUSE"
        const val ACTION_RESUME = "com.example.morningmeditation.action.RESUME"
        const val ACTION_SOUND_ON = "com.example.morningmeditation.action.SOUND_ON"
        const val ACTION_SOUND_OFF = "com.example.morningmeditation.action.SOUND_OFF"
        const val ACTION_FINISH = "com.example.morningmeditation.action.FINISH"
        const val EXTRA_DURATION = "duration"
        const val EXTRA_SOUNDSCAPE = "soundscape"
    }
}
