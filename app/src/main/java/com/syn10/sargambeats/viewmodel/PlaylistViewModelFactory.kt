package com.syn10.sargambeats.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.syn10.sargambeats.repository.PlaylistRepository


class PlaylistViewModelFactory(
    private val repository: PlaylistRepository
) : ViewModelProvider.Factory {


    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(PlaylistViewModel::class.java)) {

            @Suppress("UNCHECKED_CAST")
            return PlaylistViewModel(repository) as T

        }

        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}