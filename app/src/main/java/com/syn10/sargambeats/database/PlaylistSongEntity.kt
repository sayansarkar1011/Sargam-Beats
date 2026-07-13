package com.syn10.sargambeats.database

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "playlist_songs")
data class PlaylistSongEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val playlistId: Long,

    val songId: Long,

    val title: String,

    val artist: String,

    val duration: Long,

    val albumArt: String?,

    val dataPath: String?

)