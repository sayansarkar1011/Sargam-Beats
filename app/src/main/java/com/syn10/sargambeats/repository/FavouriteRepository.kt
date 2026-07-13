package com.syn10.sargambeats.repository

import com.syn10.sargambeats.database.FavouriteDao
import com.syn10.sargambeats.database.FavouriteEntity
import kotlinx.coroutines.flow.Flow



class FavouriteRepository(
    private val favouriteDao: FavouriteDao
) {


    val favourites: Flow<List<FavouriteEntity>> =
        favouriteDao.getAllFavourites()

    fun getAllFavourites(): Flow<List<FavouriteEntity>> {

        return favouriteDao.getAllFavourites()

    }

    suspend fun renameFavourite(
        songId: Long,
        newTitle: String
    ) {

        favouriteDao.renameFavourite(
            songId,
            newTitle
        )

    }

    suspend fun addFavourite(
        song: FavouriteEntity
    ) {

        favouriteDao.addFavourite(song)

    }


    suspend fun removeFavourite(
        song: FavouriteEntity
    ) {

        favouriteDao.removeFavourite(song)

    }


    suspend fun isFavourite(
        songId: Long
    ): Boolean {

        return favouriteDao.isFavourite(songId)

    }

}