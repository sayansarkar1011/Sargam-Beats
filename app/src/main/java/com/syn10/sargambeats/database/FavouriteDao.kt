package com.syn10.sargambeats.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavouriteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavourite(song: FavouriteEntity)


    @Delete
    suspend fun removeFavourite(song: FavouriteEntity)


    @Query("SELECT * FROM favourites")
    fun getAllFavourites(): Flow<List<FavouriteEntity>>

    @Query("UPDATE favourites SET title = :newTitle WHERE songId = :songId")
    suspend fun renameFavourite(
        songId: Long,
        newTitle: String
    )


    @Query("SELECT EXISTS(SELECT 1 FROM favourites WHERE songId = :songId)")
    suspend fun isFavourite(songId: Long): Boolean
}