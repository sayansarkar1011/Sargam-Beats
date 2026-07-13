package com.syn10.sargambeats.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syn10.sargambeats.database.PlaylistSongEntity
import com.syn10.sargambeats.repository.PlaylistRepository
import kotlinx.coroutines.launch


class PlaylistViewModel(
    private val repository: PlaylistRepository
) : ViewModel() {


    val playlists =
        repository.playlists


    fun createPlaylist(
        name: String
    ) {


        viewModelScope.launch {

            repository.createPlaylist(
                name
            )

        }

    }


    fun deletePlaylist(
        playlistId: Long
    ) {


        viewModelScope.launch {

            repository.deletePlaylist(
                playlistId
            )

        }

    }


    fun addSongToPlaylist(
        song: PlaylistSongEntity
    ) {


        viewModelScope.launch {

            repository.addSongToPlaylist(
                song
            )

        }

    }

    fun removeSongFromPlaylist(
        id: Long
    ) {


        viewModelScope.launch {


            repository.removeSongFromPlaylist(
                id
            )

        }

    }

    fun renamePlaylist(
        playlistId: Long,
        newName: String
    ) {


        viewModelScope.launch {


            repository.renamePlaylist(
                playlistId,
                newName
            )

        }

    }

    suspend fun isSongExist(
        playlistId: Long,
        songId: Long
    ): Int {


        return repository.isSongExist(
            playlistId,
            songId
        )

    }

    fun getSongCount(
        playlistId: Long
    ) =
        repository.getSongCount(
            playlistId
        )


    fun getSongsFromPlaylist(
        playlistId: Long
    ) =

        repository.getSongsFromPlaylist(
            playlistId
        )

}