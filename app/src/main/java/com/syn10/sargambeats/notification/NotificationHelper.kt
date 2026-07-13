package com.syn10.sargambeats.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import com.syn10.sargambeats.R
import com.syn10.sargambeats.service.MusicService
import com.syn10.sargambeats.ui.activity.MainActivity

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "music_channel"
        const val CHANNEL_NAME = "Music Playback"
        const val NOTIFICATION_ID = 101

        const val ACTION_PREVIOUS = "ACTION_PREVIOUS"
        const val ACTION_PLAY_PAUSE = "ACTION_PLAY_PAUSE"
        const val ACTION_NEXT = "ACTION_NEXT"
        const val ACTION_EXIT = "ACTION_EXIT"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )

            channel.description = "Music Player Controls"
            channel.setShowBadge(false)
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC

            val manager =
                context.getSystemService(NotificationManager::class.java)

            manager.createNotificationChannel(channel)
        }
    }


    fun createNotification(
        title: String,
        artist: String,
        albumArt: Bitmap?,
        isPlaying: Boolean,
        mediaSessionToken: MediaSessionCompat.Token
    ): Notification {


        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        val previousIntent = PendingIntent.getService(
            context,
            1,
            Intent(context, MusicService::class.java).apply {
                action = ACTION_PREVIOUS
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        val playPauseIntent = PendingIntent.getService(
            context,
            2,
            Intent(context, MusicService::class.java).apply {
                action = ACTION_PLAY_PAUSE
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        val nextIntent = PendingIntent.getService(
            context,
            3,
            Intent(context, MusicService::class.java).apply {
                action = ACTION_NEXT
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        val exitIntent = PendingIntent.getService(
            context,
            4,
            Intent(context, MusicService::class.java).apply {
                action = ACTION_EXIT
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        val playPauseIcon =
            if (isPlaying)
                R.drawable.outline_pause_24
            else
                R.drawable.outline_play_arrow_24


        val playPauseText =
            if (isPlaying)
                "Pause"
            else
                "Play"



        return NotificationCompat.Builder(
            context,
            CHANNEL_ID
        )
            .setSmallIcon(R.drawable.outline_music_note_24)

            .setContentTitle(title)

            .setContentText(artist)

            .setLargeIcon(
                albumArt
            )

            .setContentIntent(contentIntent)

            .setOnlyAlertOnce(true)

            .setOngoing(isPlaying)


            // LOCK SCREEN SUPPORT
            .setVisibility(
                NotificationCompat.VISIBILITY_PUBLIC
            )

            .setPriority(
                NotificationCompat.PRIORITY_HIGH
            )

            .setCategory(
                NotificationCompat.CATEGORY_TRANSPORT
            )

            .setShowWhen(false)



            .addAction(
                R.drawable.outline_skip_previous_24,
                "Previous",
                previousIntent
            )


            .addAction(
                playPauseIcon,
                playPauseText,
                playPauseIntent
            )


            .addAction(
                R.drawable.outline_skip_next_24,
                "Next",
                nextIntent
            )


            .addAction(
                R.drawable.outline_back_to_tab_24,
                "Exit",
                exitIntent
            )


            // REAL MEDIA LOCKSCREEN PLAYER
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(
                        mediaSessionToken
                    )
                    .setShowActionsInCompactView(
                        0,
                        1,
                        2
                    )
            )


            .build()
    }
}