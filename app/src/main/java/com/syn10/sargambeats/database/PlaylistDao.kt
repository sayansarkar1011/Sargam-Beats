package com.syn10.sargambeats.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface PlaylistDao {


    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    suspend fun createPlaylist(
        playlist: PlaylistEntity
    )


    @Query(
        "SELECT * FROM playlists ORDER BY name COLLATE NOCASE ASC"
    )
    fun getAllPlaylists():
            Flow<List<PlaylistEntity>>


    @Query(
        "SELECT COUNT(*) FROM playlist_songs WHERE playlistId = :playlistId"
    )
    fun getSongCount(
        playlistId: Long
    ): Flow<Int>


    @Query(
        "DELETE FROM playlists WHERE playlistId = :playlistId"
    )
    suspend fun deletePlaylist(
        playlistId: Long
    )

    @Query(
        "UPDATE playlists SET name = :newName WHERE playlistId = :playlistId"
    )
    suspend fun renamePlaylist(
        playlistId: Long,
        newName: String
    )

}