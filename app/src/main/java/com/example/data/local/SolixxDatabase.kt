package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [AnnouncementEntity::class, FriendEntity::class, LogEntity::class],
    version = 3,
    exportSchema = false
)
abstract class SolixxDatabase : RoomDatabase() {
    abstract fun announcementDao(): AnnouncementDao
    abstract fun friendDao(): FriendDao
    abstract fun logDao(): LogDao

    companion object {
        @Volatile
        private var INSTANCE: SolixxDatabase? = null

        fun getInstance(context: Context): SolixxDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SolixxDatabase::class.java,
                    "solixx_local.db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
