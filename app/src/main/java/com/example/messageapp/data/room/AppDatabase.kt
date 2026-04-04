package com.example.messageapp.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [MessageEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(MessageConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
}
