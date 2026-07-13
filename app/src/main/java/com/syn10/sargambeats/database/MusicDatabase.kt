package com.syn10.sargambeats.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(
    entities = [
        PlaylistEntity::class,
        PlaylistSongEntity::class,
        PlaylistSongCrossRef::class,
        FavouriteEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class MusicDatabase : RoomDatabase() {


    abstract fun playlistDao(): PlaylistDao

    abstract fun playlistSongDao(): PlaylistSongDao

    abstract fun favouriteDao(): FavouriteDao


    companion object {


        @Volatile
        private var INSTANCE: MusicDatabase? = null


        fun getDatabase(
            context: Context
        ): MusicDatabase {


            return INSTANCE ?: synchronized(this) {


                val instance =
                    Room.databaseBuilder(
                        context.applicationContext,
                        MusicDatabase::class.java,
                        "sargam_beats_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()


                INSTANCE =
                    instance


                instance

            }

        }

    }

}