package com.syn10.sargambeats.repository

import com.syn10.sargambeats.database.PlaylistDao
import com.syn10.sargambeats.database.PlaylistEntity
import com.syn10.sargambeats.database.PlaylistSongDao
import com.syn10.sargambeats.database.PlaylistSongEntity
import kotlinx.coroutines.flow.Flow


class PlaylistRepository(
    private val playlistDao: PlaylistDao,
    private val playlistSongDao: PlaylistSongDao
) {


    val playlists =
        playlistDao.getAllPlaylists()

    fun getSongCount(
        playlistId: Long
    ) =
        playlistDao.getSongCount(
            playlistId
        )


    suspend fun createPlaylist(
        name: String
    ) {


        playlistDao.createPlaylist(
            PlaylistEntity(
                name = name
            )
        )

    }


    suspend fun deletePlaylist(
        playlistId: Long
    ) {


        playlistDao.deletePlaylist(
            playlistId
        )

    }


    suspend fun addSongToPlaylist(
        song: PlaylistSongEntity
    ) {


        playlistSongDao.addSongToPlaylist(
            song
        )

    }

    suspend fun isSongExist(
        playlistId: Long,
        songId: Long
    ): Int {


        return playlistSongDao.isSongExist(
            playlistId,
            songId
        )

    }

    suspend fun removeSongFromPlaylist(
        id: Long
    ) {


        playlistSongDao.removeSongFromPlaylist(
            id
        )

    }

    suspend fun renamePlaylist(
        playlistId: Long,
        newName: String
    ) {


        playlistDao.renamePlaylist(
            playlistId,
            newName
        )

    }


    fun getSongsFromPlaylist(
        playlistId: Long
    ): Flow<List<PlaylistSongEntity>> {


        return playlistSongDao.getSongsFromPlaylist(
            playlistId
        )

    }

}