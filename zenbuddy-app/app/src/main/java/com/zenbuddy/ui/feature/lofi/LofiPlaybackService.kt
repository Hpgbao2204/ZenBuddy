package com.zenbuddy.ui.feature.lofi

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.zenbuddy.app.MainActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LofiPlaybackService : Service() {

    companion object {
        private val _currentTrackIndex = MutableStateFlow(-1)
        val currentTrackIndex: StateFlow<Int> = _currentTrackIndex

        private val _isPlaying = MutableStateFlow(false)
        val isPlaying: StateFlow<Boolean> = _isPlaying

        private val _isLoading = MutableStateFlow(false)
        val isLoading: StateFlow<Boolean> = _isLoading

        private val _volume = MutableStateFlow(0.7f)
        val volume: StateFlow<Float> = _volume

        private var mediaPlayerRef: MediaPlayer? = null

        const val ACTION_PLAY = "com.zenbuddy.lofi.PLAY"
        const val ACTION_STOP = "com.zenbuddy.lofi.STOP"
        const val EXTRA_TRACK_INDEX = "track_index"

        private const val CHANNEL_ID = "lofi_playback"
        private const val NOTIFICATION_ID = 1

        fun setVolume(vol: Float) {
            _volume.value = vol
            mediaPlayerRef?.setVolume(vol, vol)
        }

        fun play(context: Context, trackIndex: Int) {
            val intent = Intent(context, LofiPlaybackService::class.java).apply {
                action = ACTION_PLAY
                putExtra(EXTRA_TRACK_INDEX, trackIndex)
            }
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, LofiPlaybackService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private var mediaPlayer: MediaPlayer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                val index = intent.getIntExtra(EXTRA_TRACK_INDEX, -1)
                if (index >= 0) playTrack(index)
            }
            ACTION_STOP -> {
                stopPlayback()
                _currentTrackIndex.value = -1
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun playTrack(index: Int) {
        stopPlayback()

        // Tapping the same track that was playing → toggle off
        if (index == _currentTrackIndex.value) {
            _currentTrackIndex.value = -1
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return
        }

        _currentTrackIndex.value = index
        _isLoading.value = true

        val player = MediaPlayer().apply {
            setOnPreparedListener {
                val vol = _volume.value
                it.setVolume(vol, vol)
                it.start()
                _isPlaying.value = true
                _isLoading.value = false
                updateNotification()
            }
            setOnErrorListener { _, _, _ ->
                _isLoading.value = false
                _isPlaying.value = false
                true
            }
            setOnCompletionListener {
                _isPlaying.value = false
                updateNotification()
            }
        }
        try {
            player.setDataSource(lofiTracks[index].url)
            player.prepareAsync()
            mediaPlayer = player
            mediaPlayerRef = player
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    buildNotification(),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(NOTIFICATION_ID, buildNotification())
            }
        } catch (_: Exception) {
            _isLoading.value = false
            player.release()
        }
    }

    private fun stopPlayback() {
        mediaPlayer?.let {
            try {
                if (it.isPlaying) it.stop()
                it.reset()
                it.release()
            } catch (_: Exception) {}
        }
        mediaPlayer = null
        mediaPlayerRef = null
        _isPlaying.value = false
        _isLoading.value = false
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Lofi Music",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Background lofi music playback"
            setShowBadge(false)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val stopIntent = Intent(this, LofiPlaybackService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val trackName = if (_currentTrackIndex.value in lofiTracks.indices) {
            lofiTracks[_currentTrackIndex.value].let { "${it.emoji} ${it.title}" }
        } else "Lofi & Chill"

        val status = when {
            _isLoading.value -> "Loading..."
            _isPlaying.value -> "Now Playing"
            else -> "Stopped"
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(trackName)
            .setContentText(status)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(openPendingIntent)
            .addAction(0, "Stop", stopPendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun updateNotification() {
        try {
            getSystemService(NotificationManager::class.java)
                .notify(NOTIFICATION_ID, buildNotification())
        } catch (_: Exception) {}
    }

    override fun onDestroy() {
        stopPlayback()
        _currentTrackIndex.value = -1
        super.onDestroy()
    }
}
