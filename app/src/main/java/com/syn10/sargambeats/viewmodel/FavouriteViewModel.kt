package com.syn10.sargambeats.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syn10.sargambeats.database.FavouriteEntity
import com.syn10.sargambeats.repository.FavouriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch


class FavouriteViewModel(
    private val repository: FavouriteRepository
) : ViewModel() {


    val favourites: Flow<List<FavouriteEntity>> =
        repository.favourites


    fun addFavourite(
        song: FavouriteEntity
    ) {

        viewModelScope.launch {

            repository.addFavourite(song)

        }

    }

    fun getAllFavourites(): Flow<List<FavouriteEntity>> {

        return repository.getAllFavourites()

    }

    fun renameFavourite(
        songId: Long,
        newTitle: String
    ) {

        viewModelScope.launch {

            repository.renameFavourite(
                songId,
                newTitle
            )

        }

    }

    fun removeFavourite(
        song: FavouriteEntity
    ) {

        viewModelScope.launch {

            repository.removeFavourite(song)

        }

    }


    suspend fun isFavourite(
        songId: Long
    ): Boolean {

        return repository.isFavourite(songId)

    }

}