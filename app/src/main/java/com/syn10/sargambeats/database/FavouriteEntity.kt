package com.syn10.sargambeats.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favourites")
data class FavouriteEntity(

    @PrimaryKey
    val songId: Long,

    val title: String,

    val artist: String,

    val duration: Long,

    val albumArt: String?,

    val dataPath: String?
)