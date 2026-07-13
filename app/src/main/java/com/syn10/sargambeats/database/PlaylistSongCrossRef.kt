package com.syn10.sargambeats.database

import androidx.room.Entity

@Entity(
    tableName = "playlist_song",
    primaryKeys = ["playlistId", "songId"]
)
data class PlaylistSongCrossRef(
    val playlistId: Long,
    val songId: Long
)