package com.syn10.sargambeats.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface PlaylistSongDao {


    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    suspend fun addSongToPlaylist(
        song: PlaylistSongEntity
    )


    @Query(
        "SELECT * FROM playlist_songs WHERE playlistId = :playlistId"
    )
    fun getSongsFromPlaylist(
        playlistId: Long
    ): Flow<List<PlaylistSongEntity>>


    @Query(
        "SELECT COUNT(*) FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId"
    )
    suspend fun isSongExist(
        playlistId: Long,
        songId: Long
    ): Int


    @Query(
        "DELETE FROM playlist_songs WHERE id = :id"
    )
    suspend fun removeSongFromPlaylist(
        id: Long
    )


    @Query(
        "DELETE FROM playlist_songs WHERE playlistId = :playlistId"
    )
    suspend fun clearPlaylist(
        playlistId: Long
    )

}