package com.syn10.sargambeats.model

import android.net.Uri

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val duration: Long,
    val albumArt: Uri?,     // 👈 Uri (not String)
    val dataPath: String? = null // 👈 optional (future player use)
)