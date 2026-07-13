package com.syn10.sargambeats.service

import android.Manifest
import android.app.Notification
import android.app.Service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.syn10.sargambeats.notification.NotificationHelper
import com.syn10.sargambeats.model.Song

class MusicService : Service() {

    companion object {

        private const val REQUEST_PREV = 11
        private const val REQUEST_PLAY = 12
        private const val REQUEST_NEXT = 13
        private const val REQUEST_CONTENT = 14
        val shuffleHistory = mutableListOf<Int>()
        const val ACTION_PLAY = "PLAY"
        const val ACTION_PLAY_NEW = "PLAY_NEW"
        const val ACTION_PAUSE = "PAUSE"
        const val ACTION_NEXT = "NEXT"
        const val ACTION_PREV = "PREV"
        const val ACTION_SEEK = "SEEK"


        var songList = arrayListOf<Song>()
        var position = 0
        var mediaPlayer: MediaPlayer? = null
        var isPlaying = false

        var uiCallback: ((Song, Boolean, Int, Int) -> Unit)? = null
        var playingUiCallback: ((Song, Boolean, Int, Int) -> Unit)? = null

        var songChangeCallback: (() -> Unit)? = null

        // ================= SHUFFLE =================

        var isShuffleOn = false

        var shuffleCallback: ((Boolean) -> Unit)? = null

        var isRepeatOn = false

        var repeatCallback: (() -> Unit)? = null

        // ================= EQUALIZER =================

        var isEqualizerOn = false
        var savedBass = 0
        var savedVolumeBoost = 0

        var savedTreble = 0

        val savedBands = HashMap<Short, Short>()

        var equalizerCallback: (() -> Unit)? = null

        var equalizer: Equalizer? = null

        var bassBoost: BassBoost? = null

        var loudnessEnhancer: LoudnessEnhancer? = null


        fun notifyEqualizerState() {
            equalizerCallback?.invoke()
        }

        fun notifyRepeatState() {
            repeatCallback?.invoke()
        }

        fun notifyShuffleState() {

            shuffleCallback?.invoke(
                isShuffleOn
            )

        }
    }

    private lateinit var mediaSession: MediaSessionCompat

    private lateinit var audioManager: AudioManager

    private lateinit var notificationHelper: NotificationHelper

    private var notificationBitmap: Bitmap? = null

    // ================= AUDIO FOCUS =================

    private var resumeOnFocusGain = false

    private val audioFocusChangeListener =
        AudioManager.OnAudioFocusChangeListener { focusChange ->

            when (focusChange) {

                // CALL / REEL / OTHER AUDIO
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {

                    if (isPlaying) {
                        resumeOnFocusGain = true
                        pauseSong()
                    }
                }


                // NOTIFICATION SOUND (DUCK)
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {

                    mediaPlayer?.setVolume(
                        0.2f,
                        0.2f
                    )
                }


                // AUDIO BACK
                AudioManager.AUDIOFOCUS_GAIN -> {

                    mediaPlayer?.setVolume(
                        1f,
                        1f
                    )

                    if (resumeOnFocusGain && !isPlaying) {

                        resumeOnFocusGain = false
                        handlePlay()
                    }
                }


                // AUDIO FULL LOST
                AudioManager.AUDIOFOCUS_LOSS -> {

                    resumeOnFocusGain = false

                    if (isPlaying) {
                        pauseSong()
                    }
                }
            }
        }



    // ================= PROGRESS =================

    private val progressHandler = Handler(Looper.getMainLooper())
    private val progressRunnable = object : Runnable {

        override fun run() {

            if (isPlaying) {

                notifyUI()

                progressHandler.postDelayed(
                    this,
                    1000
                )
            }
        }
    }

    // ================= FADE =================

    private fun fadeVolume(
        from: Float,
        to: Float,
        duration: Long,
        endAction: (() -> Unit)? = null
    ) {
        val mp = mediaPlayer ?: return

        val steps = 20
        val delay = duration / steps
        val delta = (to - from) / steps
        var current = from

        val handler = Handler(Looper.getMainLooper())

        repeat(steps) { step ->

            handler.postDelayed({

                if (mediaPlayer != mp) return@postDelayed

                try {

                    current += delta

                    val volume = current.coerceIn(0f, 1f)

                    mp.setVolume(volume, volume)

                    if (step == steps - 1) {
                        endAction?.invoke()
                    }

                } catch (_: IllegalStateException) {
                }

            }, step * delay)
        }
    }

    // ================= NOISY =================

    private val noisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY == intent?.action) {
                if (isPlaying) pauseSong()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        notificationHelper = NotificationHelper(this)

        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        mediaSession = MediaSessionCompat(
            this,
            "SargamBeatsSession"
        ).apply {

            setCallback(object : MediaSessionCompat.Callback() {

                override fun onPlay() {
                    handlePlay()
                }

                override fun onPause() {
                    pauseSong()
                }

                override fun onSkipToNext() {
                    nextSong()
                }

                override fun onSkipToPrevious() {
                    prevSong()
                }

                override fun onSeekTo(pos: Long) {
                    mediaPlayer?.seekTo(pos.toInt())

                    updatePlaybackState()
                    updateNotification()
                    notifyUI()
                }
            })
            setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            )
            isActive = true
        }

        registerReceiver(
            noisyReceiver,
            IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        )
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        startForeground(
            NotificationHelper.Companion.NOTIFICATION_ID,
            buildNotification()
        )

        when (intent?.action) {

            ACTION_PLAY -> handlePlay()

            ACTION_PLAY_NEW -> playFresh()

            ACTION_PAUSE -> pauseSong()

            ACTION_NEXT -> nextSong()

            ACTION_PREV -> prevSong()

            ACTION_SEEK -> {

                val pos = intent.getIntExtra("pos", 0)

                mediaPlayer?.seekTo(pos)

                updatePlaybackState()
                updateMediaMetadata()
                updateNotification()
                notifyUI()
            }
        }
        return START_STICKY
    }

    private fun requestAudioFocus(): Boolean {

        val result = audioManager.requestAudioFocus(
            audioFocusChangeListener,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )

        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }
    // ================= CORE =================

    private fun handlePlay() {
        if (songList.isEmpty()) return
        if (!requestAudioFocus()) return

        val mp = mediaPlayer
        if (mp != null && !isPlaying) {
            mp.setVolume(0f, 0f)
            mp.start()
            isPlaying = true
            fadeVolume(0f, 1f, 400)
            updateMediaMetadata()
            updatePlaybackState()
            updateNotification()
            startProgressTicker()
            notifyUI()
        } else if (mp == null) {
            playFresh()
        }
    }

    private fun getHighQualityAlbumArt(
        path: String?
    ): Bitmap? {

        if (path == null) return null

        return try {

            val retriever = MediaMetadataRetriever()

            retriever.setDataSource(path)

            val art = retriever.embeddedPicture

            retriever.release()

            if (art != null) {

                BitmapFactory.decodeByteArray(
                    art,
                    0,
                    art.size
                )

            } else {
                null
            }

        } catch (e: Exception) {
            null
        }
    }

    private fun setupEqualizer() {

        val sessionId =
            mediaPlayer?.audioSessionId ?: return


        equalizer?.release()
        bassBoost?.release()
        loudnessEnhancer?.release()


        equalizer = Equalizer(
            0,
            sessionId
        ).apply {

            enabled = true


            for (i in 0 until numberOfBands) {

                val band = i.toShort()

                savedBands[band]?.let {

                    setBandLevel(
                        band,
                        it
                    )
                }
            }
        }


        bassBoost = BassBoost(
            0,
            sessionId
        ).apply {

            enabled = true

            setStrength(
                savedBass.toShort()
            )
        }


        loudnessEnhancer =
            LoudnessEnhancer(
                sessionId
            ).apply {

                enabled = true

                setTargetGain(
                    savedVolumeBoost
                )
            }
    }

    private fun playFresh() {

        if (songList.isEmpty()) return

        if (!requestAudioFocus()) return

        val song = songList[position]


        // clear old image
        notificationBitmap = null
        updateNotification()


        mediaPlayer?.release()

        mediaPlayer = MediaPlayer.create(
            applicationContext,
            Uri.parse(song.dataPath)
        )
        setupEqualizer()


        mediaPlayer?.setVolume(0f, 0f)
        mediaPlayer?.start()

        isPlaying = true


        fadeVolume(0f, 1f, 400)


        mediaPlayer?.setOnCompletionListener {

            if (isRepeatOn) {

                mediaPlayer?.seekTo(0)
                mediaPlayer?.start()

            } else {

                nextSong()
            }
        }


        // first try HD embedded image
        val hdArt =
            getHighQualityAlbumArt(
                song.dataPath
            )


        if (hdArt != null) {

            notificationBitmap = hdArt

            updateMediaMetadata()
            updateNotification()

        } else {

            // fallback old method
            Glide.with(this)
                .asBitmap()
                .load(song.albumArt)
                .override(1024, 1024)
                .centerCrop()
                .dontAnimate()
                .into(object : CustomTarget<Bitmap>() {

                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {

                        notificationBitmap = resource

                        updateMediaMetadata()
                        updateNotification()
                    }


                    override fun onLoadCleared(
                        placeholder: Drawable?
                    ) {

                    }
                })
        }


        updateMediaMetadata()
        updatePlaybackState()
        updateNotification()
        startProgressTicker()
        notifyUI()

        songChangeCallback?.invoke()
    }

    private fun pauseSong() {
        if (!isPlaying) return

        // update state first
        isPlaying = false

        updatePlaybackState()
        updateMediaMetadata()
        stopProgressTicker()

        // instantly update all UI
        val song = songList[position]

        uiCallback?.invoke(
            song,
            false,
            mediaPlayer?.currentPosition ?: 0,
            mediaPlayer?.duration ?: 0
        )

        playingUiCallback?.invoke(
            song,
            false,
            mediaPlayer?.currentPosition ?: 0,
            mediaPlayer?.duration ?: 0
        )

        updateNotification()


        // keep fade effect
        fadeVolume(1f, 0f, 400) {

            mediaPlayer?.pause()
        }
    }
    private fun nextSong() {

        if (songList.isEmpty()) return

        if (isShuffleOn) {

            // Current song history তে save
            shuffleHistory.add(position)

            var newPos: Int

            do {
                newPos = (songList.indices).random()
            } while (
                songList.size > 1 &&
                newPos == position
            )

            position = newPos

        } else {

            position = (position + 1) % songList.size
        }

        playFresh()
    }
    private fun prevSong() {

        if (songList.isEmpty()) return

        if (isShuffleOn) {

            if (shuffleHistory.isNotEmpty()) {

                position = shuffleHistory.removeAt(shuffleHistory.lastIndex)

                playFresh()
            }

        } else {

            position =
                if (position - 1 < 0)
                    songList.size - 1
                else
                    position - 1

            playFresh()
        }
    }

    private fun startProgressTicker() {
        progressHandler.removeCallbacks(progressRunnable)
        progressHandler.post(progressRunnable)
    }

    private fun stopProgressTicker() {
        progressHandler.removeCallbacks(progressRunnable)
    }

    private fun updatePlaybackState() {

        mediaSession.setPlaybackState(

            PlaybackStateCompat.Builder()

                .setState(
                    if (isPlaying)
                        PlaybackStateCompat.STATE_PLAYING
                    else
                        PlaybackStateCompat.STATE_PAUSED,

                    mediaPlayer?.currentPosition?.toLong() ?: 0L,

                    if (isPlaying) 1f else 0f
                )

                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or
                            PlaybackStateCompat.ACTION_PLAY_PAUSE or
                            PlaybackStateCompat.ACTION_PAUSE or
                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                            PlaybackStateCompat.ACTION_SEEK_TO
                )

                .build()
        )
    }

    private fun updateMediaMetadata() {

        val song = songList[position]

        mediaSession.setMetadata(
            MediaMetadataCompat.Builder()
                .putString(
                    MediaMetadataCompat.METADATA_KEY_TITLE,
                    song.title
                )
                .putString(
                    MediaMetadataCompat.METADATA_KEY_ARTIST,
                    song.artist
                )
                .putLong(
                    MediaMetadataCompat.METADATA_KEY_DURATION,
                    mediaPlayer?.duration?.toLong() ?: 0L
                )
                .putBitmap(
                    MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
                    notificationBitmap
                )
                .build()
        )
    }

    private fun notifyUI() {

        val mp = mediaPlayer ?: return
        val song = songList[position]

        uiCallback?.invoke(
            song,
            mp.isPlaying,
            mp.currentPosition,
            mp.duration
        )

        playingUiCallback?.invoke(
            song,
            mp.isPlaying,
            mp.currentPosition,
            mp.duration
        )

        shuffleCallback?.invoke(isShuffleOn)
        repeatCallback?.invoke()
        equalizerCallback?.invoke()
    }

    private fun buildNotification(): Notification {

        if (songList.isEmpty()) {
            return notificationHelper.createNotification(
                title = "Sargam Beats",
                artist = "",
                albumArt = null,
                isPlaying = false,
                mediaSessionToken = mediaSession.sessionToken
            )
        }

        val song = songList[position]

        return notificationHelper.createNotification(
            title = song.title,
            artist = song.artist,
            albumArt = notificationBitmap,
            isPlaying = isPlaying,
            mediaSessionToken = mediaSession.sessionToken
        )
    }

    private fun updateNotification() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        NotificationManagerCompat.from(this)
            .notify(
                NotificationHelper.Companion.NOTIFICATION_ID,
                buildNotification()
            )
    }


    override fun onDestroy() {
        unregisterReceiver(noisyReceiver)
        audioManager.abandonAudioFocus(audioFocusChangeListener)
        mediaSession.release()
        stopProgressTicker()
        mediaPlayer?.release()
        mediaPlayer = null
        isPlaying = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }
}