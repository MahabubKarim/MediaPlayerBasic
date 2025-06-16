package com.mmk.mediaplayerbasic.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mmk.mediaplayerbasic.data.local.dao.TrackDao
import com.mmk.mediaplayerbasic.data.local.entity.TrackEntity

@Database(
    entities = [TrackEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao

    companion object {
        const val DATABASE_NAME = "mediaplayer_db"
    }
}